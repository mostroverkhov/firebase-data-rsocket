package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.auth.PropsCredentialsFactory;
import com.github.mostroverkhov.firebase_rsocket.auth.ServerAuthenticator;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteResponse;
import com.google.firebase.database.*;
import com.google.gson.Gson;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class WriteRequestFuncTest {

    private Client client;
    private Completable serverStop;
    private DatabaseReference writtenRef;

    @SuppressWarnings("Duplicates")
    @Before
    public void setUp() throws Exception {

        Gson gson = new Gson();

        InetSocketAddress socketAddress = new InetSocketAddress(8090);
        ServerConfig serverConfig = new ServerConfig(
                socketAddress,
                new ServerAuthenticator(
                        new PropsCredentialsFactory("creds.properties")));
        ServerContext serverContext = new ServerContext(gson);

        ClientConfig clientConfig = new ClientConfig(socketAddress);
        ClientContext clientContext = new ClientContext(gson);
        client = new Client(clientConfig, clientContext);

        Server server = new Server(serverConfig, serverContext);
        serverStop = server.start();
    }

    @After
    public void tearDown() throws Exception {
        cleanUpWrittenValueNative();
        stopServer();
    }


    @Test
    public void writeData() throws Exception {
        Data data = new Data("w", "w");
        WriteRequest<Data> writeRequest = Requests
                .<Data>writeRequest("test", "write")
                .data(data)
                .build();

        Flowable<WriteResponse> writeResponse = client
                .write(writeRequest);

        TestSubscriber<WriteResponse> writeSubscriber
                = new TestSubscriber<>();

        writeResponse
                .observeOn(Schedulers.io())
                .subscribe(writeSubscriber);

        writeSubscriber.awaitDone(10, TimeUnit.SECONDS);
        writeSubscriber.assertValueAt(0, val -> val.getWriteKey() != null);
        writeSubscriber
                .assertNoErrors()
                .assertValueCount(1);

        writtenRef = nativeWrittenDataRef(writeSubscriber);
        assertWrittenDataNative(data, writtenRef);
    }

    private DatabaseReference nativeWrittenDataRef(TestSubscriber<WriteResponse> writeSubscriber) {
        WriteResponse wr = (WriteResponse) writeSubscriber.getEvents().get(0).get(0);
        return FirebaseDatabase.getInstance().getReference()
                .child("test").child("write")
                .child(wr.getWriteKey());
    }

    private void assertWrittenDataNative(Data data, DatabaseReference writtenRef) throws InterruptedException {
        CountDownLatch nativeReadLatch = new CountDownLatch(1);
        writtenRef
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Data receivedData = dataSnapshot.getValue(Data.class);
                        Assert.assertNotNull(receivedData);
                        Assert.assertEquals(receivedData, data);
                        nativeReadLatch.countDown();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        nativeReadLatch.countDown();
                    }
                });
        nativeReadLatch.await(10, TimeUnit.SECONDS);
    }

    private void stopServer() {
        if (serverStop != null) {
            serverStop.subscribe();
            serverStop = null;
        }
    }

    private void cleanUpWrittenValueNative() throws InterruptedException {
        if (writtenRef != null) {
            CountDownLatch nativeRemoveLatch = new CountDownLatch(1);
            writtenRef.removeValue((databaseError, databaseReference) -> nativeRemoveLatch.countDown());
            nativeRemoveLatch.await(10, TimeUnit.SECONDS);
            writtenRef = null;
        }
    }
}
