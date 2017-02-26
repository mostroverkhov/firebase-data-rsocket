package com.github.mostroverkhov.firebase_rsocket;

import com.google.gson.Gson;

import java.net.SocketAddress;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class ClientBuilder {

    private SocketAddress socketAddress;

    public ClientBuilder socketAddress(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
        return this;
    }

    public Client build() {
        if (socketAddress == null) {
            throw new IllegalArgumentException("SocketAddress should be present");
        }
        ClientConfig clientConfig = new ClientConfig(socketAddress);
        ClientContext clientContext = new ClientContext(new Gson());
        return new Client(clientConfig, clientContext);
    }
}
