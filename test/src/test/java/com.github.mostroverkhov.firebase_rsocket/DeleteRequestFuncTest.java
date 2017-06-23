package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.api.Requests;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.TypedReadResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteResponse;
import com.google.firebase.database.FirebaseDatabase;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DeleteRequestFuncTest extends AbstractTest {

    private volatile WriteResponse writeResponse;

    @Test
    public void deletePresent() throws Exception {

        Data data = new Data("w", "w");
        WriteRequest<Data> writeRequest = Requests
                .<Data>write("test", "delete")
                .data(data)
                .build();

        writeResponse = client
                .write(writeRequest)
                .subscribeOn(Schedulers.io()).blockingFirst();

        TestSubscriber<DeleteResponse> deleteTestSubscriber = new TestSubscriber<>();
        client.delete(Requests.delete(arr(writeResponse)).build())
                .observeOn(Schedulers.io())
                .subscribe(deleteTestSubscriber);
        deleteTestSubscriber.awaitDone(10, TimeUnit.SECONDS);
        deleteTestSubscriber.assertValueCount(1);
        deleteTestSubscriber.assertNoErrors();
        deleteTestSubscriber.assertComplete();

        TestSubscriber<TypedReadResponse<Data>> readTestSubscriber = new TestSubscriber<>();
        client.dataWindow(
                Requests.read("test", "delete",
                        writeResponse.getWriteKey())
                        .build())
                .flatMap(dataWindowTransformer::apply)
                .observeOn(Schedulers.io())
                .subscribe(readTestSubscriber);
        readTestSubscriber.awaitDone(10, TimeUnit.SECONDS);
        readTestSubscriber.assertValueCount(0);
        readTestSubscriber.assertNoErrors();
        readTestSubscriber.assertComplete();
    }

    @Test
    public void deleteMissing() throws Exception {

        TestSubscriber<DeleteResponse> deleteTestSubscriber = new TestSubscriber<>();
        client.delete(Requests.delete("test", "delete", "missing").build())
                .observeOn(Schedulers.io())
                .subscribe(deleteTestSubscriber);
        deleteTestSubscriber.awaitDone(10, TimeUnit.SECONDS);
        deleteTestSubscriber.assertValueCount(1);
        deleteTestSubscriber.assertNoErrors();
        deleteTestSubscriber.assertComplete();
    }

    @Override
    public void tearDown() throws Exception {
        if (writeResponse != null) {
            String writeKey = writeResponse.getWriteKey();
            CountDownLatch latch = new CountDownLatch(1);
            FirebaseDatabase.getInstance().getReference().child("test").child("delete").child(writeKey)
                    .removeValue((databaseError, databaseReference) -> {
                        latch.countDown();
                    });
            writeResponse = null;
            latch.await(10, TimeUnit.SECONDS);
        }
        super.tearDown();
    }

    private static String[] arr(WriteResponse writeResp) {
        List<String> pathChildren = writeResp.getPathChildren();
        return pathChildren.toArray(new String[pathChildren.size()]);
    }
}
