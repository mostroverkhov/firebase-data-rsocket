package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.transport.ClientTransport;
import com.google.gson.Gson;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class ClientBuilder {

    private final ClientTransport transport;

    public ClientBuilder(ClientTransport transport) {
        assertArg(transport);
        this.transport = transport;
    }

    public Client build() {
        ClientConfig clientConfig = new ClientConfig(transport, new Gson());
        return new Client(clientConfig);
    }

    private void assertArg(ClientTransport socketAddress) {
        if (socketAddress == null) {
            throw new IllegalArgumentException("SocketAddress should be present");
        }
    }
}
