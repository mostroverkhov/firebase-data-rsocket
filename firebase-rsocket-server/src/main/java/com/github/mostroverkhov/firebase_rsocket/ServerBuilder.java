package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.auth.Authenticator;
import com.github.mostroverkhov.firebase_rsocket.auth.CredentialsAuthenticator;
import com.github.mostroverkhov.firebase_rsocket.auth.PermitAllAuthenticator;
import com.github.mostroverkhov.firebase_rsocket.auth.PropsCredentialsFactory;
import com.github.mostroverkhov.firebase_rsocket.server.handler.RequestHandler;
import com.github.mostroverkhov.firebase_rsocket.server.handler.impl.UnknownHandler;
import com.github.mostroverkhov.firebase_rsocket.server.handler.impl.delete.DeleteHandler;
import com.github.mostroverkhov.firebase_rsocket.server.handler.impl.read.DataWindowHandler;
import com.github.mostroverkhov.firebase_rsocket.server.handler.impl.read.NotifRequestHandler;
import com.github.mostroverkhov.firebase_rsocket.server.handler.impl.read.cache.firebase.*;
import com.github.mostroverkhov.firebase_rsocket.server.handler.impl.write.WritePushHandler;
import com.github.mostroverkhov.firebase_rsocket.transport.ServerTransport;
import com.google.gson.Gson;

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

    private final ServerTransport transport;
    private Authenticator authenticator = PERMIT_ALL_AUTH;
    private Optional<Cache> cache = Optional.empty();

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

    public Server build() {
        ServerConfig serverConfig = new ServerConfig(
                new Gson(),
                transport,
                authenticator,
                handlers());

        return new Server(serverConfig);
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

    private List<RequestHandler<?, ?>> handlers() {

        DataWindowHandler dataWindowHandler = cache
                .map(DataWindowHandler::new)
                .orElseGet(DataWindowHandler::new);

        NotifRequestHandler notifHandler = cache
                .map(NotifRequestHandler::new)
                .orElseGet(NotifRequestHandler::new);

        return Arrays.asList(
                dataWindowHandler,
                notifHandler,
                new WritePushHandler(),
                new DeleteHandler(),
                new UnknownHandler());
    }
}
