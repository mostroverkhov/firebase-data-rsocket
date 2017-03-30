package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.auth.Authenticator;
import com.github.mostroverkhov.firebase_rsocket.server.handler.RequestHandler;
import com.github.mostroverkhov.firebase_rsocket.transport.ServerTransport;
import com.google.gson.Gson;

import java.util.List;

/**
 * Created by Maksym Ostroverkhov on 27.02.17.
 */
class ServerConfig {
    private Gson gson;
    private final ServerTransport transport;
    private final Authenticator authenticator;
    private final List<RequestHandler<?, ?>> handlers;

    public ServerConfig(Gson gson,
                        ServerTransport transport,
                        Authenticator authenticator,
                        List<RequestHandler<?, ?>> handlers) {
        this.gson = gson;
        this.transport = transport;
        this.authenticator = authenticator;
        this.handlers = handlers;
    }

    public ServerTransport transport() {
        return transport;
    }

    public Authenticator authenticator() {
        return authenticator;
    }

    public List<RequestHandler<?, ?>> handlers() {
        return handlers;
    }

    public Gson gson() {
        return gson;
    }
}
