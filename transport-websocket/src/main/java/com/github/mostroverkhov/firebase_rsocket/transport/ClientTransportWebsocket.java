package com.github.mostroverkhov.firebase_rsocket.transport;

import com.github.mostroverkhov.client.WsTransportClient;
import com.github.mostroverkhov.firebase_rsocket_data.common.transport.ClientTransport;
import io.reactivesocket.transport.TransportClient;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class ClientTransportWebsocket implements ClientTransport {

    private final SocketAddress socketAddress;

    public ClientTransportWebsocket(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    public ClientTransportWebsocket(String host, int port) {
        this.socketAddress = new InetSocketAddress(host, port);
    }

    @Override
    public TransportClient client() {
        return WsTransportClient.create(socketAddress);
    }
}
