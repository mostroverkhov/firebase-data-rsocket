package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.transport.ClientTransport;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
class ClientConfig {
    private final ClientTransport transport;

    public ClientConfig(ClientTransport transport) {
        this.transport = transport;
    }

    public ClientTransport transport() {
        return transport;
    }
}
