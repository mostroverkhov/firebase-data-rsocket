package com.github.mostroverkhov.firebase_rsocket_server;

import com.github.mostroverkhov.datawindowsource.model.DataQuery;
import com.github.mostroverkhov.firebase_data_rxjava.rx.FirebaseDatabaseManager;
import com.github.mostroverkhov.firebase_data_rxjava.rx.model.Window;
import com.github.mostroverkhov.firebase_rsocket_server.auth.Authenticator;
import com.github.mostroverkhov.firebase_rsocket_server.model.Query;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivesocket.AbstractReactiveSocket;
import io.reactivesocket.ConnectionSetupPayload;
import io.reactivesocket.Payload;
import io.reactivesocket.ReactiveSocket;
import io.reactivesocket.lease.DisabledLeaseAcceptingSocket;
import io.reactivesocket.lease.LeaseEnforcingSocket;
import io.reactivesocket.server.ReactiveSocketServer;
import io.reactivesocket.transport.TransportServer;
import io.reactivesocket.transport.tcp.server.TcpTransportServer;
import io.reactivesocket.util.PayloadImpl;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import rx.Observable;

import java.io.Reader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by Maksym Ostroverkhov on 27.02.17.
 */
public class Server {

    private final ServerConfig serverConfig;
    private SocketConfig socketConfig;

    public Server(ServerConfig serverConfig,
                  SocketConfig socketConfig) {
        this.serverConfig = serverConfig;
        this.socketConfig = socketConfig;
    }

    public Completable start() {

        TransportServer.StartedServer server = ReactiveSocketServer
                .create(TcpTransportServer.create(serverConfig.getSocketAddress()))
                .start(new ServerSocketAcceptor(
                        serverConfig.authenticator(),
                        socketConfig.gson()));

        return Completable.create(e -> {
            if (!e.isDisposed()) {
                server.shutdown();
                e.onComplete();
            }
        });
    }

    public static class SocketConfig {
        private final Gson gson;

        public SocketConfig(Gson gson) {
            this.gson = gson;
        }

        public Gson gson() {
            return gson;
        }
    }

    private static class ServerSocketAcceptor implements
            ReactiveSocketServer.SocketAcceptor {

        private Authenticator authenticator;
        private final Gson gson;

        public ServerSocketAcceptor(Authenticator authenticator, Gson gson) {
            this.authenticator = authenticator;
            this.gson = gson;
        }

        @Override
        public LeaseEnforcingSocket accept(ConnectionSetupPayload setupPayload,
                                           ReactiveSocket reactiveSocket) {
            return new DisabledLeaseAcceptingSocket(
                    new FrdDataReactiveSocket(
                            new Context(authenticator, gson)));
        }

        private static Callable<Throwable> unknownOperationError(String operation) {
            return () -> new IllegalArgumentException("Unknown operation: " + operation);
        }

        private Query query(Payload payload) {
            ByteBuffer bb = payload.getData();
            byte[] b = new byte[bb.remaining()];
            bb.get(b);
            Reader reader = new StringReader(new String(b));
            return gson.fromJson(reader, Query.class);
        }

        private class FrdDataReactiveSocket extends AbstractReactiveSocket {

            private final Context context;

            public FrdDataReactiveSocket(Context context) {
                this.context = context;
            }

            @Override
            public Publisher<Payload> requestStream(Payload payload) {
                Completable authSignal = context.authenticator().authenticate();
                Query query = query(payload);

                return authSignal
                        .andThen(QueryHandler.handler(query).handle(context, query));
            }
        }

        private static class Context {
            private Authenticator authenticator;
            private final Gson gson;

            public Context(Authenticator authenticator, Gson gson) {
                this.authenticator = authenticator;
                this.gson = gson;
            }

            public Authenticator authenticator() {
                return authenticator;
            }

            public Gson gson() {
                return gson;
            }
        }

        private enum QueryHandler {

            DATA_WINDOW {
                @Override
                boolean canHandle(Query query) {
                    return query.getOperation().equals("data_window");
                }

                @Override
                Publisher<Payload> handle(Context context, Query query) {

                    DataQuery dataQuery = toDataQuery(query);
                    Observable<Window<Object>> windowStream =
                            new FirebaseDatabaseManager(dataQuery.getDbRef())
                                    .data()
                                    .window(dataQuery);
                    Flowable<Window<Object>> windowFlow = RxJavaInterop.toV2Flowable(windowStream);
                    Flowable<Payload> payloadFlow = windowFlow
                            .map(Window::dataWindow)
                            .map(dw -> toPayload(context.gson(), dw));

                    return payloadFlow;
                }

                private Payload toPayload(Gson gson, List<Object> dw) {
                    return new PayloadImpl(gson.toJson(dw));
                }

                private DataQuery toDataQuery(Query query) {

                    DatabaseReference dataRef = FirebaseDatabase.getInstance()
                            .getReference(query.getPath().getDataPath());

                    DataQuery.Builder builder = new DataQuery.Builder(dataRef);
                    builder.windowWithSize(query.getWindowSize());
                    if (query.isAsc()) {
                        builder.asc();
                    } else {
                        builder.desc();
                    }
                    Query.OrderBy orderBy = query.getOrderBy();
                    if (orderBy == Query.OrderBy.KEY) {
                        builder.orderByKey();
                    } else if (orderBy == Query.OrderBy.VALUE) {
                        builder.orderByValue();
                    } else if (orderBy == Query.OrderBy.CHILD && query.getOrderByChildKey() != null) {
                        builder.orderByChild(query.getOrderByChildKey());
                    } else throw new IllegalStateException("Wrong order by: " + query);

                    return builder.build();
                }
            },

            UNKNOWN {
                @Override
                boolean canHandle(Query query) {
                    return true;
                }

                @Override
                Publisher<Payload> handle(Context context, Query query) {
                    return Flowable.error(unknownOperationError(query.getOperation()));
                }
            };

            abstract boolean canHandle(Query query);

            abstract Publisher<Payload> handle(Context context, Query query);

            public static QueryHandler handler(Query query) {
                return Arrays.stream(values())
                        .filter(h -> h.canHandle(query))
                        .findFirst().orElseThrow(() ->
                                new AssertionError("Handlers chain is not exhaustive"));

            }
        }
    }

}
