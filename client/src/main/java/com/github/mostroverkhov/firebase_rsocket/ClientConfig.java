package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.ClientCodec;
import com.github.mostroverkhov.firebase_rsocket_data.common.transport.ClientTransport;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
class ClientConfig {
    private final ClientTransport transport;
    private final ClientCodec codec;

    ClientConfig(ClientTransport transport, ClientCodec codec) {
        this.transport = transport;
        this.codec = codec;
    }

    public ClientTransport transport() {
        return transport;
    }

    public ClientCodec codec() {
        return codec;
    }
}
