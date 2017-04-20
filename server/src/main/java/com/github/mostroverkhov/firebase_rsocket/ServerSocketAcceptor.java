package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.auth.Authenticator;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.MetadataCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.RequestHandlers;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.ServerRequestHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.logging.LogFormatter;
import com.github.mostroverkhov.firebase_rsocket.internal.logging.Logging;
import com.github.mostroverkhov.firebase_rsocket.internal.logging.ServerFlowLogger;
import com.github.mostroverkhov.firebase_rsocket.internal.mapper.RequestMappers;
import com.github.mostroverkhov.firebase_rsocket.internal.mapper.ServerMapper;
import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;
import com.github.mostroverkhov.firebase_rsocket_data.common.BytePayload;
import com.github.mostroverkhov.firebase_rsocket_data.common.Conversions;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.github.mostroverkhov.firebase_rsocket_data.common.Conversions.dataToBytes;
import static com.github.mostroverkhov.firebase_rsocket_data.common.Conversions.metadataToBytes;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
final class ServerSocketAcceptor implements ReactiveSocketServer.SocketAcceptor {

    private final ServerConfig serverConfig;

    ServerSocketAcceptor(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public LeaseEnforcingSocket accept(ConnectionSetupPayload setupPayload,
                                       ReactiveSocket reactiveSocket) {
        return new DisabledLeaseAcceptingSocket(
                new ServerSocket(serverContext()));
    }

    private ServerContext serverContext() {
        return new ServerContext(
                serverConfig.mappers(),
                serverConfig.handlers(),
                serverConfig.metadataCodec(),
                serverConfig.authenticator(),
                serverConfig.logConfig());
    }

    private static class ServerSocket extends AbstractReactiveSocket {
        private final ServerContext context;
        private final RequestHandlers handlers;
        private final ServerMapper<?> requestMapper;
        private final Optional<Logging> logging;
        private final MetadataCodec metadataCodec;

        public ServerSocket(ServerContext context) {
            this.context = context;
            this.requestMapper = context.getRequestMappers();
            this.handlers = context.getRequestHandlers();
            this.logging = logging(context);
            this.metadataCodec = context.getMetadataCodec();
        }

        @Override
        public Publisher<Payload> requestStream(Payload payload) {

            Optional<UUID> uid = logging.map(__ -> UUID.randomUUID());
            ServerFlowLogger serverFlowLogger = new ServerFlowLogger(uid, logging);

            Flowable<BytePayload> payloadBytesFlow = Flowable
                    .fromCallable(() -> payloadBytes(payload))
                    .observeOn(Schedulers.io())
                    .cache();

            Flowable<byte[]> metadataFlow = payloadBytesFlow
                    .map(BytePayload::getMetaData);
            Flowable<KeyValue> metaDataKvFlow = metadataFlow
                    .map(metadataCodec::decode)
                    .cache();

            Flowable<Optional<MappedRequest<?>>> mappedRequestFlow =
                    payloadBytesFlow
                            .zipWith(
                                    metaDataKvFlow,
                                    (payloadBytes, metadataKv) ->
                                            mapRequest(metadataKv, payloadBytes.getData()));

            Flowable<Optional<Flowable<Payload>>> responseFlow =
                    mappedRequestFlow
                            .map(maybeMappedData -> maybeMappedData
                                    .map(serverFlowLogger::logRequest)
                                    .map(mappedData -> {
                                        Flowable<Object> response = handleRequest(mappedData);
                                        Flowable<Payload> encodedResponse = response
                                                .doOnNext(serverFlowLogger::logResponse)
                                                .map(this::encodeResponse);
                                        return encodedResponse;
                                    }));

            Flowable<Publisher<Payload>> succFlow = responseFlow
                    .filter(Optional::isPresent)
                    .map(Optional::get);

            Flowable<Publisher<Payload>> succOrErrorFlow = succFlow
                    .switchIfEmpty(metaDataKvFlow
                            .flatMap(kv -> Flowable.error(missingMapper(kv))));

            return succOrErrorFlow
                    .flatMap(__ -> __)
                    .doOnError(serverFlowLogger::logError)
                    .onErrorResumeNext(ServerSocket::resumeServerError);
        }

        private static Publisher<Payload> resumeServerError(Throwable t) {
                Throwable err = t instanceof FirebaseRsocketException
                        ? t
                        : new FirebaseRsocketException("Server error", t);
                return Flowable.error(err);
        }

        private Optional<MappedRequest<?>> mapRequest(KeyValue metadata, byte[] data) {
            Optional<?> maybeData = requestMapper.map(metadata, data);
            return maybeData.map(d -> new MappedRequest<>(metadata, d));
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

        private Flowable<Object> handleRequest(MappedRequest<?> mappedRequest) {
            return context.authenticator().authenticate()
                    .andThen(Flowable.defer(
                            () -> handlers
                                    .handlerFor(mappedRequest.getMetadata())
                                    .handleOp(mappedRequest.getMetadata(),
                                            mappedRequest.getData())));
        }

        private Payload encodeResponse(Object resp) {
            return Conversions.bytesToPayload(requestMapper.marshall(resp));
        }
    }

    private static class MappedRequest<T> {
        private final KeyValue metadata;
        private final T data;

        public MappedRequest(KeyValue metadata, T data) {
            this.metadata = metadata;
            this.data = data;
        }

        public KeyValue getMetadata() {
            return metadata;
        }

        public T getData() {
            return data;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("MappedRequest{");
            sb.append("metadata=").append(metadata);
            sb.append(", data=").append(data);
            sb.append('}');
            return sb.toString();
        }
    }

    private static FirebaseRsocketException missingMapper(KeyValue metadata) {
        return new FirebaseRsocketException("No mapper for request: " + metadata);
    }

    private static class ServerContext {
        private MetadataCodec metadataCodec;
        private final Authenticator authenticator;
        private final RequestHandlers requestHandlers;
        private final ServerMapper<?> requestMappers;
        private final Optional<LogConfig> logConfig;

        public ServerContext(List<ServerMapper<?>> requestMappers,
                             List<ServerRequestHandler<?, ?>> requestHandlers,
                             MetadataCodec metadataCodec,
                             Authenticator authenticator,
                             Optional<LogConfig> logConfig) {
            this.requestMappers = RequestMappers.newInstance(requestMappers);
            this.requestHandlers = RequestHandlers.newInstance(requestHandlers);
            this.metadataCodec = metadataCodec;
            this.authenticator = authenticator;
            this.logConfig = logConfig;
        }

        public MetadataCodec getMetadataCodec() {
            return metadataCodec;
        }

        public Authenticator authenticator() {
            return authenticator;
        }

        public RequestHandlers getRequestHandlers() {
            return requestHandlers;
        }

        public ServerMapper<?> getRequestMappers() {
            return requestMappers;
        }

        public Optional<LogConfig> getLogConfig() {
            return logConfig;
        }
    }
}
