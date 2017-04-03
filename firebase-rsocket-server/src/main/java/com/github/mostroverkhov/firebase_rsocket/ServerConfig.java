package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.auth.Authenticator;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.RequestHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.mapper.RequestMapper;
import com.github.mostroverkhov.firebase_rsocket.transport.ServerTransport;

import java.util.List;
import java.util.Optional;

/**
 * Created by Maksym Ostroverkhov on 27.02.17.
 */
class ServerConfig {
    private final ServerTransport transport;
    private final Authenticator authenticator;
    private final List<RequestHandler<?, ?>> handlers;
    private final Optional<LogConfig> logConfig;
    private RequestMapper<?> requestMapper;

    public ServerConfig(ServerTransport transport,
                        Authenticator authenticator,
                        RequestMapper<?> requestMapper,
                        List<RequestHandler<?, ?>> handlers,
                        Optional<LogConfig> logConfig) {
        this.requestMapper = requestMapper;
        this.transport = transport;
        this.authenticator = authenticator;
        this.handlers = handlers;
        this.logConfig = logConfig;
    }

    public RequestMapper<?> requestMapper() {
        return requestMapper;
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

    public Optional<LogConfig> logConfig() {
        return logConfig;
    }

}
