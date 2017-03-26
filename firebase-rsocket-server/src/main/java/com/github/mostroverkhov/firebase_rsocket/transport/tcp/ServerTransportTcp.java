package com.github.mostroverkhov.firebase_rsocket.transport.tcp;

import com.github.mostroverkhov.firebase_rsocket.transport.ServerTransport;
import io.reactivesocket.transport.TransportServer;
import io.reactivesocket.transport.tcp.server.TcpTransportServer;

import java.net.SocketAddress;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class ServerTransportTcp implements ServerTransport {

    private final SocketAddress socketAddress;

    public ServerTransportTcp(SocketAddress socketAddress) {
        if (socketAddress == null) {
            throw new IllegalArgumentException("SocketAddress should not be null");
        }
        this.socketAddress = socketAddress;
    }

    @Override
    public TransportServer transportServer() {
        return TcpTransportServer.create(socketAddress);
    }
}
