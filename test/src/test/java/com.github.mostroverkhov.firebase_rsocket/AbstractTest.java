package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.notification.NotificationTransformer;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.read.DataWindowTransformer;
import com.github.mostroverkhov.firebase_rsocket.transport.tcp.ClientTransportTcp;
import com.github.mostroverkhov.firebase_rsocket.transport.tcp.ServerTransportTcp;
import com.google.gson.Gson;
import io.reactivex.Completable;
import org.junit.After;
import org.junit.Before;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class AbstractTest {
    private static final Gson gson = new Gson();
    protected Completable serverStop;
    protected Client client;
    protected DataWindowTransformer<Data> dataWindowTransformer;
    protected NotificationTransformer<Data> notifTransformer;

    @Before
    public void setUp() throws Exception {

        dataWindowTransformer = new DataWindowTransformer<>(gson, Data.class);
        notifTransformer = new NotificationTransformer<>(gson, Data.class);

        InetSocketAddress socketAddress = new InetSocketAddress(8090);
        Server server = new ServerBuilder(
                new ServerTransportTcp(socketAddress))
                .cacheReads()
                .credentialsAuth("creds.properties")
                .build();

        Client client = new ClientBuilder(
                new ClientTransportTcp(socketAddress))
                .build();
        this.client = client;

        serverStop = server.start();
    }

    @After
    public void tearDown() throws Exception {
        stopServer();
    }

    protected void stopServer() {
        if (serverStop != null) {
            serverStop.toFlowable().subscribe();
            serverStop = null;
        }
    }

    protected void stopServerDelayed(long unit, TimeUnit timeUnit) {
        if (serverStop != null) {
            serverStop.toFlowable().delaySubscription(unit, timeUnit).subscribe();
            serverStop = null;
        }
    }
}
