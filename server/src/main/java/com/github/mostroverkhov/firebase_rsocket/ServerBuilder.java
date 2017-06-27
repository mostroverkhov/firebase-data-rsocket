package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.auth.Authenticator;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.authenticators.CredentialsAuthenticator;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.authenticators.PermitAllAuthenticator;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.sources.ClasspathPropsCredentialsSource;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.sources.FsPathPropsCredentialsSource;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.Codecs;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.DataCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.MetadataCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.GsonDataCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.GsonMetadataCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.ServerRequestHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.read.cache.firebase.*;
import com.github.mostroverkhov.firebase_rsocket.internal.mapper.ServerMapper;
import com.github.mostroverkhov.firebase_rsocket.transport.ServerTransport;
import com.google.gson.Gson;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.github.mostroverkhov.firebase_rsocket.Router.MapperHandler;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class ServerBuilder {

    private static final Cache DEFAULT_CACHE = new Cache(
            new DumbNativeCache(
                    Executors.newSingleThreadScheduledExecutor(
                            ServerBuilder::newDaemonThread)),
            new CacheDurationConstant(5, TimeUnit.SECONDS));

    private static final Gson GSON = new Gson();
    private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
    private static final Codecs GSON_CODECS = new Codecs(
            new GsonDataCodec(
                    GSON,
                    CHARSET_UTF8),
            new GsonMetadataCodec(
                    GSON,
                    CHARSET_UTF8));

    private final ServerTransport transport;
    private Authenticator authenticator;
    private Optional<Cache> cache = Optional.empty();
    private Optional<Logger> logger = Optional.empty();
    private Codecs codecs = GSON_CODECS;

    public ServerBuilder(ServerTransport transport) {
        assertTransport(transport);
        this.transport = transport;
    }

    public ServerBuilder classpathPropsAuth(String credsFile) {
        assertCredsFile(credsFile);
        this.authenticator = new CredentialsAuthenticator(
                new ClasspathPropsCredentialsSource(credsFile));
        return this;
    }

    public ServerBuilder fileSystemPropsAuth(String credsFile) {
        assertCredsFile(credsFile);
        this.authenticator = new CredentialsAuthenticator(
                new FsPathPropsCredentialsSource(credsFile));
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
        this.logger = Optional.of(logger);
        return this;
    }

    public Server build() {

        MapperHandler routes = routes();
        Queue<ServerMapper<?>> mappers = routes.mappers();
        Queue<ServerRequestHandler<?, ?>> handlers = routes.handlers();
        DataCodec dataCodec = codecs.getDataCodec();
        MetadataCodec metadataCodec = codecs.getMetadataCodec();

        setCodec(mappers, dataCodec);
        setCache(handlers, cache);

        ServerConfig serverConfig = new ServerConfig(
                transport,
                authenticator,
                mappers,
                handlers,
                metadataCodec,
                logger);

        return new Server(serverConfig);
    }

    private static void setCache(Collection<ServerRequestHandler<?, ?>> handlers,
                                 Optional<Cache> cache) {
        cache.ifPresent(c -> {
            handlers.stream()
                    .filter(h -> h instanceof HasCache)
                    .map(h -> ((HasCache) h))
                    .forEach(h -> h.setCache(c));
        });
    }

    private static void setCodec(Collection<ServerMapper<?>> mappers, DataCodec dataCodec) {
        mappers.forEach(m -> m.setDataCodec(dataCodec));
    }

    private static Thread newDaemonThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    }

    private static void assertTransport(ServerTransport transport) {
        if (transport == null) {
            throw new IllegalArgumentException("ServerTransport should be present");
        }
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

    private MapperHandler routes() {
        return Routes.router().routes();
    }
}
