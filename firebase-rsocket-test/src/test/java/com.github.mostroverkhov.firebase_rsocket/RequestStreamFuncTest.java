package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.auth.PropsCredentialsFactory;
import com.github.mostroverkhov.firebase_rsocket.auth.ServerAuthenticator;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.DataWindow;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Query;
import com.google.gson.Gson;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
public class RequestStreamFuncTest {

    private static final int SAMPLE_ITEM_COUNT = 10;

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
        Completable serverStop = server.start();

        int requestN = 1;
        Flowable<DataWindow<Data>> dataWindowFlow = client.dataWindow(query, Data.class);
        TestSubscriber<DataWindow<Data>> testSubscriber
                = new TestSubscriber<DataWindow<Data>>(requestN) {

            @Override
            public void onNext(DataWindow<Data> o) {
                super.onNext(o);
                request(requestN);
            }
        };

        dataWindowFlow
                .observeOn(Schedulers.io())
                .subscribe(testSubscriber);

        testSubscriber.awaitDone(10, TimeUnit.SECONDS);
        testSubscriber
                .assertNoErrors()
                .assertValueCount(1)
                .assertValue(
                        v -> v.getData().size() == SAMPLE_ITEM_COUNT);
        serverStop.subscribe();
    }
}
