package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.auth.Authenticator;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.DataCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.MetadataCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.ServerRequestHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.mapper.ServerMapper;
import com.github.mostroverkhov.firebase_rsocket_data.common.transport.ServerTransport;

import java.util.List;
import java.util.Optional;

/**
 * Created by Maksym Ostroverkhov on 27.02.17.
 */
class ServerConfig {
    private final ServerTransport transport;
    private final Authenticator authenticator;
    private final List<ServerRequestHandler<?, ?>> handlers;
    private final MetadataCodec metadataCodec;
    private final Optional<Logger> logConfig;
    private final List<ServerMapper<?>> mappers;

    public ServerConfig(ServerTransport transport,
                        Authenticator authenticator,
                        List<ServerMapper<?>> mappers,
                        List<ServerRequestHandler<?, ?>> handlers,
                        MetadataCodec metadataCodec,
                        Optional<Logger> logger) {
        this.transport = transport;
        this.authenticator = authenticator;
        this.mappers = mappers;
        this.handlers = handlers;
        this.metadataCodec = metadataCodec;
        this.logConfig = logger;
    }

    public ServerTransport transport() {
        return transport;
    }

    public Authenticator authenticator() {
        return authenticator;
    }

    public List<ServerMapper<?>> mappers() {
        return mappers;
    }

    public List<ServerRequestHandler<?, ?>> handlers() {
        return handlers;
    }

    public Optional<Logger> logConfig() {
        return logConfig;
    }

    public MetadataCodec metadataCodec() {
        return metadataCodec;
    }
}
