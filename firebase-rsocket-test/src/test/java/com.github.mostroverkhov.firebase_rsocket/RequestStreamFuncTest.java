package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.auth.PropsCredentialsFactory;
import com.github.mostroverkhov.firebase_rsocket.auth.ServerAuthenticator;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.DataWindow;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.ReadQuery;
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
    private static final int WINDOW_SIZE = 2;
    private static final int REQUEST_N = 1;

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

        ReadQuery readQuery = Client
                .query("test", "read")
                .asc()
                .windowWithSize(WINDOW_SIZE)
                .orderByKey()
                .build();

        Server server = new Server(serverConfig, serverContext);
        Completable serverStop = server.start();

        Flowable<DataWindow<Data>> dataWindowFlow = client.dataWindow(readQuery, Data.class);
        TestSubscriber<DataWindow<Data>> testSubscriber
                = new TestSubscriber<DataWindow<Data>>(REQUEST_N) {

            @Override
            public void onNext(DataWindow<Data> o) {
                super.onNext(o);
                request(REQUEST_N);
            }
        };

        dataWindowFlow
                .observeOn(Schedulers.io())
                .subscribe(testSubscriber);

        testSubscriber.awaitDone(10, TimeUnit.SECONDS);
        int valueCount = SAMPLE_ITEM_COUNT / WINDOW_SIZE;
        testSubscriber
                .assertNoErrors()
                .assertValueCount(valueCount);

        for (int i = 0; i < valueCount; i++) {
            int c = i;
            testSubscriber.assertValueAt(i,
                    window -> window.getData().size() == 2);
            testSubscriber.assertValueAt(i,
                    window -> assertWindowContent(window, c));
        }
        serverStop.subscribe();
    }

    private boolean assertWindowContent(DataWindow<Data> window, int index) {
        int doubledIndex = index * 2;
        for (Data data : window.getData()) {
            if (!String.valueOf(doubledIndex).equals(data.getId())) {
                return false;
            }
            doubledIndex++;
        }
        return true;
    }
}
