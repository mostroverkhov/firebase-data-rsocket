package com.github.mostroverkhov.firebase_rsocket.transport;

import com.github.mostroverkhov.firebase_rsocket_data.common.transport.ServerTransport;
import com.github.mostroverkhov.server.WsTransportServer;
import io.reactivesocket.transport.TransportServer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class ServerTransportWebsocket implements ServerTransport {

    private final SocketAddress socketAddress;

    public ServerTransportWebsocket(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    public ServerTransportWebsocket(String host, int port) {
        this.socketAddress = new InetSocketAddress(host, port);
    }

    @Override
    public TransportServer transportServer() {
        return WsTransportServer.create(socketAddress);
    }
}
