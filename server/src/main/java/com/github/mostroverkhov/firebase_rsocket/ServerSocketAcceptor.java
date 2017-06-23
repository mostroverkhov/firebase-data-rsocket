package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.servercommon.BytePayload;
import com.github.mostroverkhov.firebase_rsocket.servercommon.KeyValue;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.Authenticator;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.MetadataCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.ServerHandlers;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.ServerRequestHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.logging.LogFormatter;
import com.github.mostroverkhov.firebase_rsocket.internal.logging.Logging;
import com.github.mostroverkhov.firebase_rsocket.internal.logging.LogsSupport;
import com.github.mostroverkhov.firebase_rsocket.internal.mapper.ServerMapper;
import com.github.mostroverkhov.firebase_rsocket.internal.mapper.ServerMappers;
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
import java.util.Queue;

import static com.github.mostroverkhov.firebase_rsocket.servercommon.Conversions.bytesToPayload;
import static com.github.mostroverkhov.firebase_rsocket.servercommon.Conversions.dataToBytes;
import static com.github.mostroverkhov.firebase_rsocket.servercommon.Conversions.metadataToBytes;


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
                new ServerSocket(socketConfig()));
    }

    private SocketConfig socketConfig() {
        return new SocketConfig(
                serverConfig.mappers(),
                serverConfig.handlers(),
                serverConfig.metadataCodec(),
                serverConfig.authenticator(),
                serverConfig.logger());
    }

    private static class ServerSocket extends AbstractReactiveSocket {
        private final SocketConfig context;
        private final ServerHandlers handlers;
        private final ServerMapper<?> requestMapper;
        private final MetadataCodec metadataCodec;
        private final LogsSupport logSupport;

        public ServerSocket(SocketConfig context) {
            this.context = context;
            this.requestMapper = context.getRequestMappers();
            this.handlers = context.getRequestHandlers();
            this.logSupport = new LogsSupport(logging(context.getLogger()));
            this.metadataCodec = context.getMetadataCodec();
        }

        @Override
        public Publisher<Payload> requestStream(Payload payload) {

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
                                    .map(logSupport::logRequest)
                                    .map(mappedData -> {
                                        Flowable<Object> response = handleRequest(mappedData);
                                        Flowable<Payload> encodedResponse = response
                                                .doOnNext(logSupport::logResponse)
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
                    .doOnError(logSupport::logError)
                    .onErrorResumeNext(ServerSocket::resumeServerError);
        }

        private static Publisher<Payload> resumeServerError(Throwable t) {
            Throwable err = t instanceof FirebaseRsocketException
                    ? t
                    : new FirebaseRsocketException("Server error " + t, t);
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

        private static Optional<Logging> logging(Optional<Logger> logger) {
            return logger
                    .map(l ->
                            new Logging(
                                    l,
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
            return bytesToPayload(requestMapper.marshall(resp));
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

    private static class SocketConfig {
        private MetadataCodec metadataCodec;
        private final Authenticator authenticator;
        private final ServerHandlers requestHandlers;
        private final ServerMapper<?> requestMappers;
        private final Optional<Logger> logConfig;

        public SocketConfig(Queue<ServerMapper<?>> requestMappers,
                            Queue<ServerRequestHandler<?, ?>> requestHandlers,
                            MetadataCodec metadataCodec,
                            Authenticator authenticator,
                            Optional<Logger> logConfig) {
            this.requestMappers = ServerMappers.newInstance(requestMappers);
            this.requestHandlers = ServerHandlers.newInstance(requestHandlers);
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

        public ServerHandlers getRequestHandlers() {
            return requestHandlers;
        }

        public ServerMapper<?> getRequestMappers() {
            return requestMappers;
        }

        public Optional<Logger> getLogger() {
            return logConfig;
        }
    }
}
