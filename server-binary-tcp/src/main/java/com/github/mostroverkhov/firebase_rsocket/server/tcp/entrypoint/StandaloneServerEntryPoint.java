package com.github.mostroverkhov.firebase_rsocket.server.tcp.entrypoint;

import com.github.mostroverkhov.firebase_rsocket.Server;
import io.reactivex.Completable;

import java.util.Optional;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class StandaloneServerEntryPoint {

    public static void main(String[] args) {
        Configuration config = new ArgsReader().read(args);
        Server server = new StandaloneServerConfigurer().configure(config);
        server.start();
        Completable.never().blockingAwait();
    }
}
