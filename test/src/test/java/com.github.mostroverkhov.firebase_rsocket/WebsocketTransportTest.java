package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.api.Client;
import com.github.mostroverkhov.firebase_rsocket.api.Requests;
import com.github.mostroverkhov.firebase_rsocket.api.Transform;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.read.TypedReadResponse;
import com.github.mostroverkhov.firebase_rsocket.transport.ClientTransportWebsocket;
import com.github.mostroverkhov.firebase_rsocket.transport.ServerTransportWebsocket;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class WebsocketTransportTest {

    private Completable serverStop;
    private Client client;
    private Transform transform;

    @Before
    public void setUp() throws Exception {
        InetSocketAddress socketAddress = new InetSocketAddress("localhost", 8090);
        Server server = new ServerBuilder(
                new ServerTransportWebsocket(socketAddress))
                .cacheReads()
                .classpathPropsAuth("creds.properties")
                .build();

        ClientFactory clientFactory = new ClientBuilder(
                new ClientTransportWebsocket(socketAddress))
                .build();
        this.client = clientFactory.client();
        this.transform = clientFactory.transform();


        serverStop = server.start();
    }

    @SuppressWarnings("Duplicates")
    @Test
    public void transportRead() throws Exception {

        ReadRequest readRequest = presentReadRequest();
        Flowable<TypedReadResponse<Data>> dataWindowFlow = client
                .dataWindow(readRequest).flatMap(r -> transform.dataWindowOf(Data.class).from(r));
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

    private void stopServer() {
        if (serverStop != null) {
            serverStop.toFlowable().subscribe();
            serverStop = null;
        }
    }
}
