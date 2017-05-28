package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.Serializer;
import com.github.mostroverkhov.firebase_rsocket_data.common.transport.ClientTransport;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
class ClientConfig {
    private final ClientTransport transport;
    private final Serializer serializer;

    ClientConfig(ClientTransport transport, Serializer serializer) {
        this.transport = transport;
        this.serializer = serializer;
    }

    public ClientTransport transport() {
        return transport;
    }

    public <T extends Serializer> T serializer() {
        return (T) serializer;
    }
}
