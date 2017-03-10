package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.datawindowsource.model.DataQuery;
import com.github.mostroverkhov.firebase_data_rxjava.rx.FirebaseDatabaseManager;
import com.github.mostroverkhov.firebase_data_rxjava.rx.model.Window;
import com.github.mostroverkhov.firebase_data_rxjava.rx.model.WriteResult;
import com.github.mostroverkhov.firebase_rsocket.auth.Authenticator;
import com.github.mostroverkhov.firebase_rsocket.handlers.DefaultHandlerAdapter;
import com.github.mostroverkhov.firebase_rsocket.handlers.RequestHandlerAdapter;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.*;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteResponse;
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

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Created by Maksym Ostroverkhov on 27.02.17.
 */
public class Server {

    private final ServerConfig serverConfig;
    private final ServerContext serverContext;

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
                    new FirebaseReactiveSocket(
                            new SocketContext(authenticator, gson),
                            new DefaultHandlerAdapter(gson)));
        }

        private static Callable<Throwable> unknownOperationError(String operation) {
            String msg = operation.isEmpty() ? " empty" : operation;
            return () -> new FirebaseRsocketMessageFormatException(
                    "No handler for operation: " + msg);
        }

        private static class FirebaseReactiveSocket extends AbstractReactiveSocket {

            private final SocketContext context;
            private RequestHandlerAdapter<?> requestHandlerAdapter;

            public FirebaseReactiveSocket(SocketContext context,
                                          RequestHandlerAdapter<?> requestHandlerAdapter) {
                this.context = context;
                this.requestHandlerAdapter = requestHandlerAdapter;
            }

            @Override
            public Publisher<Payload> requestStream(Payload payload) {
                Completable authSignal = context.authenticator().authenticate();
                String request = request(payload);
                Optional<?> maybeRequest = requestHandlerAdapter.adapt(request);
                if (!maybeRequest.isPresent()) {
                    return Flowable.error(requestMissingHandlerAdapter(request));
                } else {
                    Operation adaptedRequest = (Operation) maybeRequest.get();
                    return authSignal
                            .andThen(Flowable.defer(
                                    () -> {
                                        RequestHandler handler = RequestHandler
                                                .handler(adaptedRequest);
                                        return handler
                                                .handle(context, adaptedRequest);
                                    }));
                }
            }

            private static String request(Payload payload) {
                ByteBuffer bb = payload.getData();
                byte[] b = new byte[bb.remaining()];
                bb.get(b);
                return new String(b);
            }
        }

        private static FirebaseRsocketMessageFormatException
        requestMissingHandlerAdapter(String request) {
            return new FirebaseRsocketMessageFormatException(
                    "No handler adapter for request: " + request);
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

        private enum RequestHandler {

            WRITE_PUSH {
                @Override
                boolean canHandle(Operation op) {
                    return op.getOp().equals("write_push");
                }

                @Override
                Publisher<Payload> handle(final SocketContext context,
                                          final Operation op) {

                    WriteRequest writeRequest = (WriteRequest) op;

                    Path path = writeRequest.getPath();
                    DatabaseReference dbRef = getReference(path);
                    DatabaseReference newKeyRef = dbRef.push();

                    Object data = writeRequest.getData();
                    Observable<WriteResult>
                            writeResultO = new FirebaseDatabaseManager(newKeyRef)
                            .data()
                            .setValue(data);

                    Flowable<WriteResult> writeResultFlow = RxJavaInterop
                            .toV2Flowable(writeResultO);
                    Flowable<Payload> payloadFlow = writeResultFlow
                            .map(writeResult -> writeResponse(path, newKeyRef))
                            .map(resp -> toPayload(context.gson(), resp));

                    return payloadFlow;
                }

                private WriteResponse writeResponse(Path path,
                                                    DatabaseReference newKeyRef) {
                    return new WriteResponse(newKeyRef.getKey(),
                            path.getChildPaths());
                }

                private Payload toPayload(Gson gson, Object dw) {
                    String data = gson.toJson(dw);
                    return new PayloadImpl(data);
                }
            },

            DATA_WINDOW {
                @Override
                boolean canHandle(Operation op) {
                    return op.getOp().equals("data_window");
                }

                @Override
                Publisher<Payload> handle(final SocketContext context,
                                          final Operation op) {

                    ReadRequest readRequest = (ReadRequest) op;
                    DataQuery dataQuery = toDataQuery(readRequest);
                    Observable<Window<Object>> windowStream =
                            new FirebaseDatabaseManager(dataQuery.getDbRef())
                                    .data()
                                    .window(dataQuery);
                    Flowable<Window<Object>> windowFlow = RxJavaInterop
                            .toV2Flowable(windowStream);
                    Flowable<Payload> payloadFlow = windowFlow
                            .map(window -> new ReadResponse<>(
                                    readRequest,
                                    window.dataWindow()))
                            .map(dw -> toPayload(context.gson(), dw));

                    return payloadFlow;
                }

                private Payload toPayload(Gson gson,
                                          ReadResponse<Object> dw) {
                    String data = gson.toJson(dw);
                    return new PayloadImpl(data);
                }

                private DataQuery toDataQuery(ReadRequest readRequest) {

                    Path path = readRequest.getPath();
                    DatabaseReference dataRef = getReference(path);

                    DataQuery.Builder builder = new DataQuery.Builder(dataRef);
                    builder.windowWithSize(readRequest.getWindowSize());
                    if (readRequest.isAsc()) {
                        builder.asc();
                    } else {
                        builder.desc();
                    }
                    ReadRequest.OrderBy orderBy = readRequest.getOrderBy();
                    if (orderBy == ReadRequest.OrderBy.KEY) {
                        builder.orderByKey();
                    } else if (orderBy == ReadRequest.OrderBy.VALUE) {
                        builder.orderByValue();
                    } else if (orderBy == ReadRequest.OrderBy.CHILD
                            && readRequest.getOrderByChildKey() != null) {
                        builder.orderByChild(readRequest.getOrderByChildKey());
                    } else throw new IllegalStateException("Wrong order by: " + readRequest);

                    return builder.build();
                }
            },

            UNKNOWN {
                @Override
                boolean canHandle(Operation readQuery) {
                    return true;
                }

                @Override
                Publisher<Payload> handle(SocketContext context,
                                          Operation op) {
                    return Flowable.error(unknownOperationError(op.getOp()));
                }
            };

            private static DatabaseReference getReference(Path path) {
                DatabaseReference dataRef = FirebaseDatabase.getInstance()
                        .getReference();
                for (String s : Arrays.asList(path.getChildPaths())) {
                    dataRef = dataRef.child(s);
                }
                return dataRef;
            }

            abstract boolean canHandle(Operation operation);

            abstract Publisher<Payload> handle(SocketContext context,
                                               Operation operation);

            public static RequestHandler handler(Operation request) {
                return Arrays.stream(values())
                        .filter(h -> h.canHandle(request))
                        .findFirst().orElseThrow(() ->
                                new AssertionError(
                                        "Handlers chain is not exhaustive"));

            }
        }
    }
}
