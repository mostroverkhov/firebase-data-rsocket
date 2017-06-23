package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.ClientCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.GsonClientCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.GsonSerializer;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.JsonAsStringTypeAdapter;
import com.github.mostroverkhov.firebase_rsocket_data.common.transport.ClientTransport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class ClientFactoryBuilder {
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

    public ClientFactoryBuilder(ClientTransport transport) {
        assertArg(transport);
        this.transport = transport;
    }

    public ClientFactory build() {
        return new ClientFactory(transport, codec);
    }

    private void assertArg(ClientTransport socketAddress) {
        if (socketAddress == null) {
            throw new IllegalArgumentException("SocketAddress should be present");
        }
    }
}
