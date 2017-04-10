package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.auth.Authenticator;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.HandlerManager;
import com.github.mostroverkhov.firebase_rsocket.internal.logging.LogFormatter;
import com.github.mostroverkhov.firebase_rsocket.internal.logging.Logging;
import com.github.mostroverkhov.firebase_rsocket.internal.logging.ServerFlowLogger;
import com.github.mostroverkhov.firebase_rsocket.internal.mapper.ServerRequestMapper;
import com.github.mostroverkhov.firebase_rsocket_data.common.BytePayload;
import com.github.mostroverkhov.firebase_rsocket_data.common.Conversions;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;
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
import java.util.UUID;

import static com.github.mostroverkhov.firebase_rsocket_data.common.Conversions.*;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
final class ServerSocketAcceptor implements ReactiveSocketServer.SocketAcceptor {

    private ServerConfig serverConfig;

    ServerSocketAcceptor(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public LeaseEnforcingSocket accept(ConnectionSetupPayload setupPayload,
                                       ReactiveSocket reactiveSocket) {
        return new DisabledLeaseAcceptingSocket(
                new ServerReactiveSocket(serverContext()));
    }

    private ServerContext serverContext() {
        return new ServerContext(
                serverConfig.requestMapper(),
                new HandlerManager(serverConfig.handlers()),
                serverConfig.authenticator(),
                serverConfig.logConfig());
    }

    private static class ServerReactiveSocket extends AbstractReactiveSocket {
        private final ServerContext context;
        private final HandlerManager handlerManager;
        private final ServerRequestMapper<?> requestMapper;
        private final Optional<Logging> logging;

        public ServerReactiveSocket(ServerContext context) {
            this.context = context;
            this.handlerManager = context.getHandlerManager();
            this.requestMapper = context.getRequestMapper();
            this.logging = logging(context);
        }

        @Override
        public Publisher<Payload> requestStream(Payload payload) {

            Optional<UUID> uid = logging.map(__ -> UUID.randomUUID());
            ServerFlowLogger serverFlowLogger = new ServerFlowLogger(uid, logging);

            Flowable<BytePayload> payloadBytesFlow = Flowable.fromCallable(() -> payloadBytes(payload))
                    .observeOn(Schedulers.io())
                    .cache();

            Flowable<Optional<Publisher<Payload>>> responseFlow = payloadBytesFlow
                    .map(plBytes -> {
                        Optional<? extends Operation> operation = mapRequest(plBytes);
                        return operation
                                .map(serverFlowLogger::logRequest)
                                .map(op -> {
                                    Flowable<Object> response = handleRequest(op);
                                    return response
                                            .doOnNext(serverFlowLogger::logResponse)
                                            .map(this::payload);
                                });
                    });
            Flowable<Publisher<Payload>> succFlow = responseFlow
                    .filter(Optional::isPresent)
                    .map(Optional::get);
            Flowable<Publisher<Payload>> succOrErrorFlow = succFlow
                    .switchIfEmpty(payloadBytesFlow
                            .flatMap(r -> {
                                String request = bytesToString(r.getData());
                                return Flowable
                                        .<Publisher<Payload>>error(missingHandlerMapper(request))
                                        .doOnError(serverFlowLogger::logError);
                            }));
            return succOrErrorFlow.flatMap(pub -> pub);
        }

        private Optional<? extends Operation> mapRequest(BytePayload plBytes) {
            byte[] metadata = plBytes.getMetaData();
            byte[] data = plBytes.getData();
            return requestMapper.map(
                    metadata,
                    data);
        }

        private static BytePayload payloadBytes(Payload pl) {
            byte[] metadata = metadataToBytes(pl);
            byte[] data = dataToBytes(pl);
            return new BytePayload(metadata, data);
        }

        private static Optional<Logging> logging(ServerContext context) {
            return context
                    .getLogConfig()
                    .map(config ->
                            new Logging(
                                    config.getLogger(),
                                    new LogFormatter()));
        }

        private Flowable<Object> handleRequest(Operation operation) {
            return context.authenticator().authenticate()
                    .andThen(Flowable.defer(
                            () -> handlerManager
                                    .handler(operation)
                                    .handleOp(operation)));
        }

        private Payload payload(Object resp) {
            return Conversions.bytesToPayload(requestMapper.marshall(resp));
        }

    }

    private static FirebaseRsocketMessageFormatException missingHandlerMapper(String request) {
        return new FirebaseRsocketMessageFormatException("No mapper for request: " + request);
    }

    public static class ServerContext {
        private final Authenticator authenticator;
        private final HandlerManager handlerManager;
        private final ServerRequestMapper<?> requestMapper;
        private final Optional<LogConfig> logConfig;

        public ServerContext(ServerRequestMapper<?> requestMapper,
                             HandlerManager handlerManager,
                             Authenticator authenticator,
                             Optional<LogConfig> logConfig) {
            this.authenticator = authenticator;
            this.handlerManager = handlerManager;
            this.requestMapper = requestMapper;
            this.logConfig = logConfig;

        }

        public Authenticator authenticator() {
            return authenticator;
        }

        public HandlerManager getHandlerManager() {
            return handlerManager;
        }

        public ServerRequestMapper<?> getRequestMapper() {
            return requestMapper;
        }

        public Optional<LogConfig> getLogConfig() {
            return logConfig;
        }
    }
}
