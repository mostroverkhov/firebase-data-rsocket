package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.auth.Authenticator;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.CredentialsAuthenticator;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.PermitAllAuthenticator;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.PropsCredentialsFactory;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.DataCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.GsonDataCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.GsonMetadataCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.MetadataCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.ServerRequestHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.impl.UnknownHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.impl.delete.DeleteHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.impl.read.DataWindowHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.impl.read.NotifRequestHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.impl.read.cache.firebase.*;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.impl.write.WritePushHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.mapper.OperationRequestMapper;
import com.github.mostroverkhov.firebase_rsocket.internal.mapper.ServerRequestMapper;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.transport.ServerTransport;
import com.google.gson.Gson;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class ServerBuilder {

    private static final Authenticator PERMIT_ALL_AUTH = new PermitAllAuthenticator();
    private static final Cache DEFAULT_CACHE = new Cache(
            new DumbNativeCache(
                    Executors.newSingleThreadScheduledExecutor(
                            ServerBuilder::newDaemonThread)),
            new CacheDurationConstant(5, TimeUnit.SECONDS));
    private static final Codecs GSON_CODECS = new Codecs(
            new GsonDataCodec(
                    new Gson(),
                    Charset.forName("UTF-8")),
            new GsonMetadataCodec());

    private final ServerTransport transport;
    private Authenticator authenticator = PERMIT_ALL_AUTH;
    private Optional<Cache> cache = Optional.empty();
    private Optional<LogConfig> logConfig = Optional.empty();
    private Codecs codecs = GSON_CODECS;

    public ServerBuilder(ServerTransport transport) {
        assertTransport(transport);
        this.transport = transport;
    }

    public ServerBuilder noAuth() {
        this.authenticator = PERMIT_ALL_AUTH;
        return this;
    }

    public ServerBuilder credentialsAuth(String credsFile) {
        assertCredsFile(credsFile);
        this.authenticator = new CredentialsAuthenticator(
                new PropsCredentialsFactory(credsFile));
        return this;
    }

    public ServerBuilder codecs(Codecs codecs) {
        assertNotNull(codecs);
        this.codecs = codecs;
        return this;
    }

    public ServerBuilder cacheReads() {
        cache = Optional.of(DEFAULT_CACHE);
        return this;
    }

    public ServerBuilder cacheReads(NativeCache nativeCache,
                                    CacheDuration cacheDuration) {
        assertNotNull(nativeCache, cacheDuration);
        cache = Optional.of(new Cache(nativeCache, cacheDuration));
        return this;
    }

    public ServerBuilder noCacheReads() {
        cache = Optional.empty();
        return this;
    }

    public ServerBuilder logging(Logger logger) {
        assertNotNull(logger);
        logConfig = Optional.of(new LogConfig(logger));
        return this;
    }

    public Server build() {
        ServerConfig serverConfig = new ServerConfig(
                transport,
                authenticator,
                mappers(),
                handlers(),
                codecs.getDataCodec(),
                codecs.getMetadataCodec(),
                logConfig);

        return new Server(serverConfig);
    }

    public static class Codecs {
        private final DataCodec dataCodec;
        private final MetadataCodec metadataCodec;

        public Codecs(DataCodec dataCodec,
                      MetadataCodec metadataCodec) {
            this.dataCodec = dataCodec;
            this.metadataCodec = metadataCodec;
        }

        public DataCodec getDataCodec() {
            return dataCodec;
        }

        public MetadataCodec getMetadataCodec() {
            return metadataCodec;
        }
    }

    private void assertTransport(ServerTransport transport) {
        if (transport == null) {
            throw new IllegalArgumentException("ServerTransport should be present");
        }
    }

    private static Thread newDaemonThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    }

    private static void assertCredsFile(String credsFile) {
        if (credsFile == null || credsFile.isEmpty()) {
            throw new IllegalArgumentException("Credentials file should be present");
        }
    }

    private static void assertNotNull(Object... args) {
        for (Object arg : args) {
            if (arg == null) {
                throw new IllegalArgumentException("Args should not be null: " + Arrays.toString(args));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<ServerRequestHandler<?, ?>> handlers() {

        return Arrays.asList(
                new DataWindowHandler(cache),
                new NotifRequestHandler(cache),
                new WritePushHandler(),
                new DeleteHandler(),
                new UnknownHandler());
    }

    private List<ServerRequestMapper<?>> mappers() {

        DataCodec dataCodec = codecs.getDataCodec();
        return Arrays.asList(
                new OperationRequestMapper<>(dataCodec, ReadRequest.class, Op.DATA_WINDOW.code()),
                new OperationRequestMapper<>(dataCodec, ReadRequest.class, Op.DATA_WINDOW_NOTIF.code()),
                new OperationRequestMapper<>(dataCodec, WriteRequest.class, Op.WRITE_PUSH.code()),
                new OperationRequestMapper<>(dataCodec, DeleteRequest.class, Op.DELETE.code())
        );
    }
}
