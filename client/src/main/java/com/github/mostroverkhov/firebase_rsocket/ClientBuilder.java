package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.ClientCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.GsonClientCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.util.GsonSerializer;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.util.JsonAsStringTypeAdapter;
import com.github.mostroverkhov.firebase_rsocket_data.common.transport.ClientTransport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class ClientBuilder {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(
                    new JsonAsStringTypeAdapter
                            .JsonAsStringTypeAdapterFactory()
            )
            .create();
    private static final ClientCodec codec =
            new GsonClientCodec(
                    new GsonSerializer(gson, "UTF-8")
            );
    private final ClientTransport transport;

    public ClientBuilder(ClientTransport transport) {
        assertArg(transport);
        this.transport = transport;
    }

    public Client build() {
        ClientConfig clientConfig = new ClientConfig(transport, codec);
        return new Client(clientConfig);
    }

    private void assertArg(ClientTransport socketAddress) {
        if (socketAddress == null) {
            throw new IllegalArgumentException("SocketAddress should be present");
        }
    }
}
