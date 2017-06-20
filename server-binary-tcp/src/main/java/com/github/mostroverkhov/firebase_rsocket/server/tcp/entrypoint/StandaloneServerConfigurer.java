package com.github.mostroverkhov.firebase_rsocket.server.tcp.entrypoint;

import com.github.mostroverkhov.firebase_rsocket.Server;
import com.github.mostroverkhov.firebase_rsocket.ServerBuilder;
import com.github.mostroverkhov.firebase_rsocket.transport.tcp.ServerTransportTcp;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
class StandaloneServerConfigurer {

    public Server configure(Configuration conf) {
        return configureServerBuilder(conf).build();
    }

    static ServerBuilder configureServerBuilder(Configuration configuration) {
        Optional<String> serverPort = configuration.getPort();
        Optional<String> credsFile = configuration.getCredsFile();
        int port = serverPort.map(StandaloneServerConfigurer::parsePort)
                .orElseThrow(
                        () -> new IllegalArgumentException("server.port property missing")
                );
        InetSocketAddress socketAddress = new InetSocketAddress(port);

        ServerBuilder serverBuilder = new ServerBuilder(
                new ServerTransportTcp(socketAddress))
                .cacheReads();

        if (credsFile.isPresent()) {
            serverBuilder.fileSystemPropsAuth(credsFile.get());
        } else {
            serverBuilder.noAuth();
        }
        return serverBuilder;
    }

    static int parsePort(String serverPort) {
        try {
            return Integer.parseInt(serverPort);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Port: integer expected");
        }
    }
}
