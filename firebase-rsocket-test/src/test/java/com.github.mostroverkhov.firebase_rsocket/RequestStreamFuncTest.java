package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.auth.PropsCredentialsFactory;
import com.github.mostroverkhov.firebase_rsocket.auth.ServerAuthenticator;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.DataWindow;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Query;
import com.google.gson.Gson;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.junit.Assert;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
public class RequestStreamFuncTest {

    @Test
    public void requestStream() throws Exception {

        Gson gson = new Gson();

        InetSocketAddress socketAddress = new InetSocketAddress(8090);
        ServerConfig serverConfig = new ServerConfig(
                socketAddress,
                new ServerAuthenticator(
                        new PropsCredentialsFactory("creds.properties")));
        ServerContext serverContext = new ServerContext(gson);

        ClientConfig clientConfig = new ClientConfig(socketAddress);
        ClientContext clientContext = new ClientContext(gson);
        Client client = new Client(clientConfig, clientContext);
        Query query = Client
                .query("test", "read")
                .asc()
                .windowWithSize(42)
                .orderByKey()
                .build();

        Server server = new Server(serverConfig, serverContext);
        Completable stopHandle = server.start();

        Flowable<DataWindow<Data>> dataWindowFlow = client.dataWindow(query, Data.class);

        CountDownLatch latch = new CountDownLatch(1);

        dataWindowFlow
                .observeOn(Schedulers.io())
                .subscribe(new Subscriber<DataWindow<Data>>() {

                    private volatile Subscription subscription;
                    private final List<Data> items = Collections.synchronizedList(
                            new ArrayList<>());

                    @Override
                    public void onSubscribe(Subscription subscription) {
                        this.subscription = subscription;
                        this.subscription.request(1);
                    }

                    @Override
                    public void onNext(DataWindow<Data> objectDataWindow) {
                        Assert.assertNotNull(objectDataWindow);
                        items.addAll(objectDataWindow.getData());
                        subscription.request(1);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {
                        Assert.assertEquals(10, items.size());
                        latch.countDown();
                    }
                });

        boolean success = latch.await(10, TimeUnit.SECONDS);
        Assert.assertTrue("Test should not timeout", success);

        stopHandle.subscribe();
    }
}
