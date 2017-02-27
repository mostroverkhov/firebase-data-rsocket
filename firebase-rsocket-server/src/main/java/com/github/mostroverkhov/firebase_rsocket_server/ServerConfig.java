package com.github.mostroverkhov.firebase_rsocket_server;

import com.github.mostroverkhov.firebase_rsocket_server.auth.Authenticator;

import java.net.SocketAddress;

/**
 * Created by Maksym Ostroverkhov on 27.02.17.
 */
public class ServerConfig {
    private final SocketAddress socketAddress;
    private final Authenticator authenticator;

    public ServerConfig(SocketAddress socketAddress,
                        Authenticator authenticator) {
        this.socketAddress = socketAddress;
        this.authenticator = authenticator;
    }

    public SocketAddress getSocketAddress() {
        return socketAddress;
    }

    public Authenticator authenticator() {
        return authenticator;
    }
}
