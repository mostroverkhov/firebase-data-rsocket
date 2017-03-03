package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.datawindowsource.model.DataQuery;
import com.github.mostroverkhov.firebase_data_rxjava.rx.FirebaseDatabaseManager;
import com.github.mostroverkhov.firebase_data_rxjava.rx.model.Window;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.DataWindow;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Path;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.ReadQuery;
import com.github.mostroverkhov.firebase_rsocket.auth.Authenticator;
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
import java.util.concurrent.Callable;

/**
 * Created by Maksym Ostroverkhov on 27.02.17.
 */
public class Server {

    private final ServerConfig serverConfig;
    private ServerContext serverContext;

    public Server(ServerConfig serverConfig,
                  ServerContext serverContext) {
        this.serverConfig = serverConfig;
        this.serverContext = serverContext;
    }

    public Completable start() {

        TransportServer.StartedServer server = ReactiveSocketServer
                .create(TcpTransportServer.create(serverConfig.getSocketAddress()))
                .start(new ServerSocketAcceptor(
                        serverConfig.authenticator(),
                        serverContext.gson()));

        return Completable.create(e -> {
            if (!e.isDisposed()) {
                server.shutdown();
                e.onComplete();
            }
        });
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
                            new SocketContext(authenticator, gson)));
        }

        private static Callable<Throwable> unknownOperationError(String operation) {
            String msg = operation.isEmpty() ? " empty" : operation;
            return () -> new FirebaseRsocketMessageFormatException(
                    "Unknown operation: " + msg);
        }

        private ReadQuery query(Payload payload) {
            ByteBuffer bb = payload.getData();
            byte[] b = new byte[bb.remaining()];
            bb.get(b);
            Reader reader = new StringReader(new String(b));
            try {
                ReadQuery readQuery = getQuery(reader);
                return readQuery;
            } catch (Exception e) {
                throw new FirebaseRsocketMessageFormatException("Payload is not a Query", e);
            }
        }

        private ReadQuery getQuery(Reader reader) {
            return gson.fromJson(reader, ReadQuery.class);
        }

        private class FrdDataReactiveSocket extends AbstractReactiveSocket {

            private final SocketContext context;

            public FrdDataReactiveSocket(SocketContext context) {
                this.context = context;
            }

            @Override
            public Publisher<Payload> requestStream(Payload payload) {
                Completable authSignal = context.authenticator().authenticate();
                ReadQuery readQuery = query(payload);

                return authSignal
                        .andThen(Flowable.defer(
                                () -> QueryHandler.handler(readQuery).handle(context, readQuery)));
            }
        }

        private static class SocketContext {
            private Authenticator authenticator;
            private final Gson gson;

            public SocketContext(Authenticator authenticator, Gson gson) {
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
                boolean canHandle(ReadQuery readQuery) {
                    return readQuery.getOperation().equals("data_window");
                }

                @Override
                Publisher<Payload> handle(final SocketContext context, final ReadQuery readQuery) {

                    DataQuery dataQuery = toDataQuery(readQuery);
                    Observable<Window<Object>> windowStream =
                            new FirebaseDatabaseManager(dataQuery.getDbRef())
                                    .data()
                                    .window(dataQuery);
                    Flowable<Window<Object>> windowFlow = RxJavaInterop.toV2Flowable(windowStream);
                    Flowable<Payload> payloadFlow = windowFlow
                            .map(window -> new DataWindow<>(readQuery, window.dataWindow()))
                            .map(dw -> toPayload(context.gson(), dw));

                    return payloadFlow;
                }

                private Payload toPayload(Gson gson, DataWindow<Object> dw) {
                    String data = gson.toJson(dw);
                    return new PayloadImpl(data);
                }
            },

            UNKNOWN {
                @Override
                boolean canHandle(ReadQuery readQuery) {
                    return true;
                }

                @Override
                Publisher<Payload> handle(SocketContext context, ReadQuery readQuery) {
                    return Flowable.error(unknownOperationError(readQuery.getOperation()));
                }
            };

            abstract boolean canHandle(ReadQuery readQuery);

            abstract Publisher<Payload> handle(SocketContext context, ReadQuery readQuery);

            public static QueryHandler handler(ReadQuery readQuery) {
                return Arrays.stream(values())
                        .filter(h -> h.canHandle(readQuery))
                        .findFirst().orElseThrow(() ->
                                new AssertionError("Handlers chain is not exhaustive"));

            }

            private static DataQuery toDataQuery(ReadQuery readQuery) {

                Path path = readQuery.getPath();
                DatabaseReference dataRef = FirebaseDatabase.getInstance()
                        .getReference();
                for (String s : Arrays.asList(path.getChildPaths())) {
                    dataRef = dataRef.child(s);
                }

                DataQuery.Builder builder = new DataQuery.Builder(dataRef);
                builder.windowWithSize(readQuery.getWindowSize());
                if (readQuery.isAsc()) {
                    builder.asc();
                } else {
                    builder.desc();
                }
                ReadQuery.OrderBy orderBy = readQuery.getOrderBy();
                if (orderBy == ReadQuery.OrderBy.KEY) {
                    builder.orderByKey();
                } else if (orderBy == ReadQuery.OrderBy.VALUE) {
                    builder.orderByValue();
                } else if (orderBy == ReadQuery.OrderBy.CHILD && readQuery.getOrderByChildKey() != null) {
                    builder.orderByChild(readQuery.getOrderByChildKey());
                } else throw new IllegalStateException("Wrong order by: " + readQuery);

                return builder.build();
            }
        }
    }
}
