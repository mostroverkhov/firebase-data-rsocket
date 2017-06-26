package com.github.mostroverkhov.firebase_rsocket.server.tcp.entrypoint;

import com.github.mostroverkhov.firebase_rsocket.Server;
import com.github.mostroverkhov.firebase_rsocket.ServerBuilder;
import com.github.mostroverkhov.firebase_rsocket.transport.tcp.ServerTransportTcp;

import java.net.InetSocketAddress;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
class StandaloneServerConfigurer {

    public Server configure(Configuration conf) {
        return configureServerBuilder(conf).build();
    }

    static ServerBuilder configureServerBuilder(Configuration configuration) {
        Integer serverPort = configuration.getPort();
        String credsFile = configuration.getCredsFile();
        InetSocketAddress socketAddress = new InetSocketAddress(serverPort);
        ServerBuilder serverBuilder = new ServerBuilder(
                new ServerTransportTcp(socketAddress))
                .cacheReads()
                .fileSystemPropsAuth(credsFile);

        return serverBuilder;
    }
}
