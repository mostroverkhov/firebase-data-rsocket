package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.codec.gson.GsonTransform;
import com.github.mostroverkhov.firebase_rsocket.api.Transform;
import com.github.mostroverkhov.firebase_rsocket.codec.ClientCodec;
import com.github.mostroverkhov.firebase_rsocket.codec.gson.GsonClientCodec;
import com.github.mostroverkhov.firebase_rsocket.codec.gson.JsonAsStringTypeAdapter;
import com.github.mostroverkhov.firebase_rsocket.transport.ClientTransport;
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
            ).create();
    private static final ClientCodec codec =
            new GsonClientCodec(gson,
                    "UTF-8");
    private static final Transform transform =
            new GsonTransform(gson);

    private final ClientTransport transport;

    public ClientBuilder(ClientTransport transport) {
        assertArg(transport);
        this.transport = transport;
    }

    public ClientFactory build() {
        return new ClientFactory(transport,
                codec,
                transform);
    }

    private void assertArg(ClientTransport socketAddress) {
        if (socketAddress == null) {
            throw new IllegalArgumentException("SocketAddress should be present");
        }
    }
}
