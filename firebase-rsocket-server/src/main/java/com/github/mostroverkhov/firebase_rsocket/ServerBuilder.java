package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.auth.Authenticator;
import com.github.mostroverkhov.firebase_rsocket.auth.PermitAllAuthenticator;
import com.github.mostroverkhov.firebase_rsocket.auth.PropsCredentialsFactory;
import com.github.mostroverkhov.firebase_rsocket.auth.CredentialsAuthenticator;
import com.github.mostroverkhov.firebase_rsocket.handlers.cache.firebase.CacheDuration;
import com.github.mostroverkhov.firebase_rsocket.handlers.cache.firebase.CacheDurationConstant;
import com.github.mostroverkhov.firebase_rsocket.handlers.cache.firebase.DumbNativeCache;
import com.github.mostroverkhov.firebase_rsocket.handlers.cache.firebase.NativeCache;
import com.github.mostroverkhov.firebase_rsocket.handlers.requesthandlers.RequestHandler;
import com.github.mostroverkhov.firebase_rsocket.handlers.requesthandlers.impl.DataWindowHandler;
import com.github.mostroverkhov.firebase_rsocket.handlers.requesthandlers.impl.DeleteHandler;
import com.github.mostroverkhov.firebase_rsocket.handlers.requesthandlers.impl.UnknownHandler;
import com.github.mostroverkhov.firebase_rsocket.handlers.requesthandlers.impl.WritePushHandler;
import com.google.gson.Gson;

import java.lang.annotation.Native;
import java.net.SocketAddress;
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
                    Executors.newSingleThreadScheduledExecutor()),
            new CacheDurationConstant(5, TimeUnit.SECONDS));

    private SocketAddress socketAddress;
    private Authenticator authenticator = PERMIT_ALL_AUTH;

    private Optional<DataWindowHandler.Cache> cache;

    public ServerBuilder socketAddress(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
        return this;
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
        if (socketAddress == null) {
            throw new IllegalArgumentException("SocketAddress should be present");
        }
        ServerConfig serverConfig = new ServerConfig(
                socketAddress,
                authenticator,
                handlers());

        ServerContext serverContext = new ServerContext(new Gson());

        return new Server(serverConfig, serverContext);
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
