package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.auth.Authenticator;
import com.github.mostroverkhov.firebase_rsocket.handlers.cache.firebase.CacheDuration;
import com.github.mostroverkhov.firebase_rsocket.handlers.cache.firebase.NativeCache;
import com.github.mostroverkhov.firebase_rsocket.handlers.requesthandlers.RequestHandler;

import java.net.SocketAddress;
import java.util.List;
import java.util.Optional;

/**
 * Created by Maksym Ostroverkhov on 27.02.17.
 */
class ServerConfig {
    private final SocketAddress socketAddress;
    private final Authenticator authenticator;
    private final List<RequestHandler> handlers;

    public ServerConfig(SocketAddress socketAddress,
                        Authenticator authenticator,
                        List<RequestHandler> handlers) {
        this.socketAddress = socketAddress;
        this.authenticator = authenticator;
        this.handlers = handlers;
    }

    public SocketAddress getSocketAddress() {
        return socketAddress;
    }

    public Authenticator authenticator() {
        return authenticator;
    }

    public List<RequestHandler> handlers() {
        return handlers;
    }

}
