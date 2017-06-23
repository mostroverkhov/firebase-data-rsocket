package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.api.Client;
import com.github.mostroverkhov.firebase_rsocket.api.Requests;
import com.github.mostroverkhov.firebase_rsocket.api.gson.transformers.read.DataWindowTransformer;
import com.github.mostroverkhov.firebase_rsocket.transport.tcp.ClientTransportTcp;
import com.github.mostroverkhov.firebase_rsocket.transport.tcp.ServerTransportTcp;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.TypedReadResponse;
import com.google.gson.Gson;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class ClientFactorySmokeTest {

    private static final Gson gson = new Gson();
    protected Completable serverStop;
    protected Client client;
    protected DataWindowTransformer<Data> dataWindowTransformer;

    @Before
    public void setUp() throws Exception {

        dataWindowTransformer = new DataWindowTransformer<>(gson, Data.class);

        InetSocketAddress socketAddress = new InetSocketAddress(8090);
        Server server = new ServerBuilder(
                new ServerTransportTcp(socketAddress))
                .cacheReads()
                .classpathPropsAuth("creds.properties")
                .build();

        ClientFactory clientFactory = new ClientFactoryBuilder(
                new ClientTransportTcp(socketAddress))
                .build();
        this.client = clientFactory.client(Client.class);
        serverStop = server.start();
    }

    @After
    public void tearDown() throws Exception {
        stopServer();
    }

    private void stopServer() {
        if (serverStop != null) {
            serverStop.toFlowable().subscribe();
            serverStop = null;
        }
    }

    @Test
    public void clientFactory() throws Exception {
        InetSocketAddress socketAddress = new InetSocketAddress(8090);
        ReadRequest request = Requests
                .read("test", "read")
                .asc()
                .windowWithSize(2)
                .orderByKey()
                .build();

        Flowable<ReadResponse> resp = client.dataWindow(request);
        TypedReadResponse<Data> response = resp.observeOn(Schedulers.io())
                .flatMap(dataWindowTransformer::apply)
                .blockingFirst();
        Assert.assertNotNull(response);
        List<Data> data = response.getData();
        Assert.assertNotNull(data);
        Assert.assertEquals(2, data.size());
        data.forEach(d -> {
            Assert.assertNotNull(d);
            Assert.assertNotNull(d.getId());
            Assert.assertNotNull(d.getData());
        });
    }
}
