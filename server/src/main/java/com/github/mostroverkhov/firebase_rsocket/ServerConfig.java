package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.auth.Authenticator;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.MetadataCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.ServerRequestHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.mapper.ServerMapper;
import com.github.mostroverkhov.firebase_rsocket.transport.ServerTransport;

import java.util.Optional;
import java.util.Queue;

/**
 * Created by Maksym Ostroverkhov on 27.02.17.
 */
class ServerConfig {
    private final ServerTransport transport;
    private final Authenticator authenticator;
    private final Queue<ServerRequestHandler<?, ?>> handlers;
    private final MetadataCodec metadataCodec;
    private final Optional<Logger> logger;
    private final Queue<ServerMapper<?>> mappers;

    public ServerConfig(ServerTransport transport,
                        Authenticator authenticator,
                        Queue<ServerMapper<?>> mappers,
                        Queue<ServerRequestHandler<?, ?>> handlers,
                        MetadataCodec metadataCodec,
                        Optional<Logger> logger) {
        this.transport = transport;
        this.authenticator = authenticator;
        this.mappers = mappers;
        this.handlers = handlers;
        this.metadataCodec = metadataCodec;
        this.logger = logger;
    }

    public ServerTransport transport() {
        return transport;
    }

    public Authenticator authenticator() {
        return authenticator;
    }

    public Queue<ServerMapper<?>> mappers() {
        return mappers;
    }

    public Queue<ServerRequestHandler<?, ?>> handlers() {
        return handlers;
    }

    public Optional<Logger> logger() {
        return logger;
    }

    public MetadataCodec metadataCodec() {
        return metadataCodec;
    }
}
