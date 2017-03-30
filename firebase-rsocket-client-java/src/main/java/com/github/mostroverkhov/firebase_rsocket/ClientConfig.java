package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.transport.ClientTransport;
import com.google.gson.Gson;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
class ClientConfig {
    private final ClientTransport transport;
    private final Gson gson;

    public ClientConfig(ClientTransport transport, Gson gson) {
        this.transport = transport;
        this.gson = gson;
    }

    public ClientTransport transport() {
        return transport;
    }

    public Gson gson() {
        return gson;
    }
}
