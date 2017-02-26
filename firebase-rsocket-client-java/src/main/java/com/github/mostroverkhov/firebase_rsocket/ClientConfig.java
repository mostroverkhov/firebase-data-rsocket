package com.github.mostroverkhov.firebase_rsocket;

import java.net.SocketAddress;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
class ClientConfig {
    private final SocketAddress socketAddress;

    public ClientConfig(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    public SocketAddress getSocketAddress() {
        return socketAddress;
    }
}
