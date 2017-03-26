package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.auth.Authenticator;
import com.github.mostroverkhov.firebase_rsocket.auth.CredentialsAuthenticator;
import com.github.mostroverkhov.firebase_rsocket.auth.PermitAllAuthenticator;
import com.github.mostroverkhov.firebase_rsocket.auth.PropsCredentialsFactory;
import com.github.mostroverkhov.firebase_rsocket.handlers.cache.firebase.CacheDuration;
import com.github.mostroverkhov.firebase_rsocket.handlers.cache.firebase.CacheDurationConstant;
import com.github.mostroverkhov.firebase_rsocket.handlers.cache.firebase.DumbNativeCache;
import com.github.mostroverkhov.firebase_rsocket.handlers.cache.firebase.NativeCache;
import com.github.mostroverkhov.firebase_rsocket.handlers.requesthandlers.RequestHandler;
import com.github.mostroverkhov.firebase_rsocket.handlers.requesthandlers.impl.DataWindowHandler;
import com.github.mostroverkhov.firebase_rsocket.handlers.requesthandlers.impl.DeleteHandler;
import com.github.mostroverkhov.firebase_rsocket.handlers.requesthandlers.impl.UnknownHandler;
import com.github.mostroverkhov.firebase_rsocket.handlers.requesthandlers.impl.WritePushHandler;
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
    private static final DataWindowHandler.Cache DEFAULT_CACHE = new DataWindowHandler.Cache(
            new DumbNativeCache(
                    Executors.newSingleThreadScheduledExecutor(
                            ServerBuilder::newDaemonThread)),
            new CacheDurationConstant(5, TimeUnit.SECONDS));

    private final ServerTransport transport;
    private Authenticator authenticator = PERMIT_ALL_AUTH;
    private Optional<DataWindowHandler.Cache> cache = Optional.empty();

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
        cache = Optional.of(new DataWindowHandler.Cache(nativeCache, cacheDuration));
        return this;
    }

    public ServerBuilder noCacheReads() {
        cache = Optional.empty();
        return this;
    }

    public Server build() {
        ServerConfig serverConfig = new ServerConfig(
                transport,
                authenticator,
                handlers());

        ServerContext serverContext = new ServerContext(new Gson());

        return new Server(serverConfig, serverContext);
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

    private List<RequestHandler> handlers() {

        DataWindowHandler dataWindowHandler = cache
                .map(DataWindowHandler::new)
                .orElseGet(DataWindowHandler::new);

        return Arrays.asList(
                dataWindowHandler,
                new WritePushHandler(),
                new DeleteHandler(),
                new UnknownHandler());
    }
}
