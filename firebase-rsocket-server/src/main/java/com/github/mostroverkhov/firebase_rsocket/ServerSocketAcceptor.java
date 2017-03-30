package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.auth.Authenticator;
import com.github.mostroverkhov.firebase_rsocket.server.handler.HandlerManager;
import com.github.mostroverkhov.firebase_rsocket.server.handler.impl.HandlerCommon;
import com.github.mostroverkhov.firebase_rsocket.server.mapper.DefaultRequestMapper;
import com.github.mostroverkhov.firebase_rsocket.server.mapper.RequestMapper;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;
import com.google.gson.Gson;
import io.reactivesocket.AbstractReactiveSocket;
import io.reactivesocket.ConnectionSetupPayload;
import io.reactivesocket.Payload;
import io.reactivesocket.ReactiveSocket;
import io.reactivesocket.lease.DisabledLeaseAcceptingSocket;
import io.reactivesocket.lease.LeaseEnforcingSocket;
import io.reactivesocket.server.ReactiveSocketServer;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Publisher;

import java.util.Optional;

import static com.github.mostroverkhov.firebase_rsocket_data.common.Conversions.bytes;
import static com.github.mostroverkhov.firebase_rsocket_data.common.Conversions.bytesToString;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class ServerSocketAcceptor implements ReactiveSocketServer.SocketAcceptor {

    private ServerConfig serverConfig;
    private final HandlerManager handlerManager;

    ServerSocketAcceptor(ServerConfig serverConfig,
                         HandlerManager handlerManager) {
        this.serverConfig = serverConfig;
        this.handlerManager = handlerManager;
    }

    @Override
    public LeaseEnforcingSocket accept(ConnectionSetupPayload setupPayload,
                                       ReactiveSocket reactiveSocket) {
        return new DisabledLeaseAcceptingSocket(
                new FirebaseReactiveSocket(
                        new RsocketContext(new DefaultRequestMapper(serverConfig.gson()),
                                handlerManager,
                                serverConfig.authenticator(),
                                serverConfig.gson())
                        ));
    }

    private static class FirebaseReactiveSocket extends AbstractReactiveSocket {
        private final RsocketContext context;
        private final HandlerManager handlerManager;
        private final RequestMapper<?> requestMapper;

        public FirebaseReactiveSocket(RsocketContext context) {
            this.context = context;
            this.handlerManager = context.getHandlerManager();
            this.requestMapper = context.getRequestMapper();
        }

        @Override
        public Publisher<Payload> requestStream(Payload payload) {

            Flowable<byte[]> requestFlow = Flowable.fromCallable(() -> bytes(payload))
                    .observeOn(Schedulers.io())
                    .cache();

            Flowable<Optional<Publisher<Payload>>> responseFlow = requestFlow
                    .map(request ->
                            requestMapper
                                    .map(request)
                                    .map(this::handleRequest));
            Flowable<Publisher<Payload>> succFlow = responseFlow
                    .filter(Optional::isPresent)
                    .map(Optional::get);
            Flowable<Publisher<Payload>> succOrErrorFlow = succFlow
                    .switchIfEmpty(requestFlow
                            .flatMap(r -> Flowable
                                    .error(missingHandlerMapper(bytesToString(r)))));

            return succOrErrorFlow.flatMap(pub -> pub);
        }

        private Publisher<Payload> handleRequest(Operation operation) {
            return context.authenticator().authenticate()
                    .andThen(Flowable.defer(
                            () -> handlerManager
                                    .handler(operation)
                                    .handleOp(operation)
                                    .map(this::payload)));
        }

        private Payload payload(Object resp) {
            return HandlerCommon.payload(context.gson(), resp);
        }

    }

    private static FirebaseRsocketMessageFormatException
    missingHandlerMapper(String request) {
        return new FirebaseRsocketMessageFormatException(
                "No handler mapper for request: " + request);
    }

    public static class RsocketContext {
        private Authenticator authenticator;
        private final Gson gson;
        private final HandlerManager handlerManager;
        private final RequestMapper<?> requestMapper;


        public RsocketContext(RequestMapper<?> requestMapper,
                              HandlerManager handlerManager,
                              Authenticator authenticator,
                              Gson gson) {
            this.authenticator = authenticator;
            this.gson = gson;
            this.handlerManager = handlerManager;
            this.requestMapper = requestMapper;
        }

        public Authenticator authenticator() {
            return authenticator;
        }

        public Gson gson() {
            return gson;
        }

        public HandlerManager getHandlerManager() {
            return handlerManager;
        }

        public RequestMapper<?> getRequestMapper() {
            return requestMapper;
        }
    }

}
