package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.api.Client;
import com.github.mostroverkhov.firebase_rsocket.api.Transform;
import com.github.mostroverkhov.firebase_rsocket.api.gson.transformers.Transformer;
import com.github.mostroverkhov.firebase_rsocket.transport.tcp.ClientTransportTcp;
import com.github.mostroverkhov.firebase_rsocket.transport.tcp.ServerTransportTcp;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NotifResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.TypedNotifResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.TypedReadResponse;
import io.reactivex.Completable;
import io.reactivex.Flowable;
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
    protected Transformer<ReadResponse, Flowable<TypedReadResponse<Data>>> dataWindowTransformer;
    protected Transformer<NotifResponse, Flowable<TypedNotifResponse<Data>>> notifTransformer;

    @Before
    public void setUp() throws Exception {
        InetSocketAddress socketAddress = new InetSocketAddress(8090);
        Server server = new ServerBuilder(
                new ServerTransportTcp(socketAddress))
                .cacheReads()
                .classpathPropsAuth("creds.properties")
                .build();

        ClientFactory clientFactory = new ClientBuilder(
                new ClientTransportTcp(socketAddress))
                .build();
        this.client = clientFactory
                .client(Client.class);
        Transform transform = clientFactory.transform();

        notifTransformer = transform.notificationsOf(Data.class);
        dataWindowTransformer = transform.dataWindowOf(Data.class);
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
