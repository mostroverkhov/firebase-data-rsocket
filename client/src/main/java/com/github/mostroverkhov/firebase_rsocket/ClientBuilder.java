package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.Serializer;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.GsonSerializer;
import com.github.mostroverkhov.firebase_rsocket_data.common.transport.ClientTransport;
import com.google.gson.Gson;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class ClientBuilder {

    private static final Serializer serializer =
            new GsonSerializer(
                    new Gson(),
                    "UTF-8");
    private final ClientTransport transport;

    public ClientBuilder(ClientTransport transport) {
        assertArg(transport);
        this.transport = transport;
    }

    public Client build() {
        ClientConfig clientConfig = new ClientConfig(transport, serializer);
        return new Client(clientConfig);
    }

    private void assertArg(ClientTransport socketAddress) {
        if (socketAddress == null) {
            throw new IllegalArgumentException("SocketAddress should be present");
        }
    }
}
