package com.github.mostroverkhov.firebase_rsocket;

import io.reactivesocket.server.ReactiveSocketServer;
import io.reactivesocket.transport.TransportServer;
import io.reactivex.Completable;

import java.util.concurrent.TimeUnit;

/**
 * Created by Maksym Ostroverkhov on 27.02.17.
 */
public class Server {

    private final ServerConfig serverConfig;

    public Server(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public Completable start() {

        TransportServer.StartedServer server = ReactiveSocketServer
                .create(serverConfig.transport().transportServer())
                .start(new ServerSocketAcceptor(serverConfig));

        return Completable.create(e -> {
            if (!e.isDisposed()) {
                server.shutdown();
                e.onComplete();
            }
        });
    }
}
