package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.handlers.requesthandlers.HandlerManager;
import io.reactivesocket.server.ReactiveSocketServer;
import io.reactivesocket.transport.TransportServer;
import io.reactivesocket.transport.tcp.server.TcpTransportServer;
import io.reactivex.Completable;

/**
 * Created by Maksym Ostroverkhov on 27.02.17.
 */
public class Server {

    private final ServerConfig serverConfig;
    private final ServerContext serverContext;

    public Server(ServerConfig serverConfig,
                  ServerContext serverContext) {
        this.serverConfig = serverConfig;
        this.serverContext = serverContext;
    }

    public Completable start() {

        TransportServer.StartedServer server = ReactiveSocketServer
                .create(TcpTransportServer.create(serverConfig.getSocketAddress()))
                .start(
                        new ServerSocketAcceptor(
                                serverConfig.authenticator(),
                                new HandlerManager(serverConfig.handlers()),
                                serverContext.gson()));

        return Completable.create(e -> {
            if (!e.isDisposed()) {
                server.shutdown();
                e.onComplete();
            }
        });
    }
}
