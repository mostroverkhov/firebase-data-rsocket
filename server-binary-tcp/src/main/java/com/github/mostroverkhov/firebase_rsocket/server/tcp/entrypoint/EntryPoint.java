package com.github.mostroverkhov.firebase_rsocket.server.tcp.entrypoint;

import com.github.mostroverkhov.firebase_rsocket.Server;
import com.github.mostroverkhov.firebase_rsocket.ServerBuilder;
import com.github.mostroverkhov.firebase_rsocket.transport.tcp.ServerTransportTcp;
import io.reactivex.Completable;

import java.net.InetSocketAddress;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class EntryPoint {

    public static void main(String[] args) {
        String serverPort = serverPort();
        int port = parsePort(serverPort);
        InetSocketAddress socketAddress = new InetSocketAddress(port);
        Server server = new ServerBuilder(
                new ServerTransportTcp(socketAddress))
                .cacheReads()
                .credentialsAuth("creds.properties")
                .build();

        server.start();
        Completable.never().blockingAwait();
    }

    private static String serverPort() {
        String prop = System.getProperty("server.port");
        if (prop == null) {
            throw new IllegalArgumentException("server.port property missing");
        }
        return prop;
    }

    private static int parsePort(String serverPort) {
        try {
            return Integer.parseInt(serverPort);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Port: integer expected");
        }
    }
}
