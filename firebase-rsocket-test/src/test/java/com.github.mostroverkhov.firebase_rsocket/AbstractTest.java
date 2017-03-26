package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.transport.tcp.ClientTransportTcp;
import com.github.mostroverkhov.firebase_rsocket.transport.tcp.ServerTransportTcp;
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

    protected Completable serverStop;
    protected Client client;

    @Before
    public void setUp() throws Exception {
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
