package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.api.Client;
import com.github.mostroverkhov.firebase_rsocket.api.Requests;
import com.github.mostroverkhov.firebase_rsocket.codec.gson.transformers.notification.NotificationTransformer;
import com.github.mostroverkhov.firebase_rsocket.codec.gson.transformers.read.DataWindowTransformer;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.read.TypedReadResponse;
import com.github.mostroverkhov.firebase_rsocket.transport.aeron.AeronDriver;
import com.github.mostroverkhov.firebase_rsocket.transport.aeron.ClientTransportAeron;
import com.github.mostroverkhov.firebase_rsocket.transport.aeron.ServerTransportAeron;
import com.google.gson.Gson;
import io.reactivesocket.aeron.internal.reactivestreams.AeronSocketAddress;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class AeronTransportTest {

    private Client client;
    private Completable serverStop;

    private static final Gson gson = new Gson();
    protected DataWindowTransformer<Data> dataWindowTransformer;
    protected NotificationTransformer<Data> notifTransformer;

    @Before
    public void setUp() throws Exception {

        dataWindowTransformer = new DataWindowTransformer<>(gson, Data.class);
        notifTransformer = new NotificationTransformer<>(gson, Data.class);

        AeronDriver.load();
        AeronSocketAddress aeronSocketAddress = AeronSocketAddress
                .create(
                        "aeron:udp",
                        "127.0.0.1",
                        8091);
        Server server = new ServerBuilder(
                new ServerTransportAeron(aeronSocketAddress))
                .cacheReads()
                .classpathPropsAuth("creds.properties")
                .build();

        client = new ClientBuilder(
                new ClientTransportAeron(aeronSocketAddress))
                .build()
                .client();

        serverStop = server.start();
    }

    @Test
    public void readRequestFuncTest() throws Exception {

        ReadRequest readRequest = presentReadRequest();
        Flowable<TypedReadResponse<Data>> dataWindowFlow = client
                .dataWindow(readRequest).flatMap(dataWindowTransformer::from);
        TestSubscriber<TypedReadResponse<Data>> testSubscriber
                = requestStreamSubscriber();

        dataWindowFlow
                .observeOn(Schedulers.io())
                .subscribe(testSubscriber);

        int itemCount = DataFixture.ITEM_COUNT;
        testSubscriber.awaitDone(itemCount * 2, TimeUnit.SECONDS);
        testSubscriber
                .assertNoErrors()
                .assertValueCount(1)
                .assertValueAt(0, resp -> resp.getData().size() == itemCount);

    }

    private TestSubscriber<TypedReadResponse<Data>> requestStreamSubscriber() {
        return new TestSubscriber<TypedReadResponse<Data>>(1) {
            @Override
            public void onNext(TypedReadResponse<Data> o) {
                super.onNext(o);
                request(1);
            }
        };
    }

    private ReadRequest presentReadRequest() {
        return Requests
                .read("test", "read")
                .asc()
                .windowWithSize(42)
                .orderByKey()
                .build();
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

}
