package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.model.delete.DeleteResponse;
import com.github.mostroverkhov.firebase_rsocket.model.read.TypedReadResponse;
import com.github.mostroverkhov.firebase_rsocket.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket.model.write.WriteResponse;
import com.github.mostroverkhov.firebase_rsocket.requests.Req;
import com.google.firebase.database.FirebaseDatabase;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Test;
import reactor.core.scheduler.Schedulers;

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
        WriteRequest<Data> writeRequest = Req
                .<Data>write("test", "delete")
                .data(data)
                .build();

        FirebaseService svc = client.request();
        writeResponse = svc
                .write(writeRequest)
                .subscribeOn(Schedulers.elastic()).blockFirst();

        TestSubscriber<DeleteResponse> deleteTestSubscriber = new TestSubscriber<>();
        svc.delete(Req.delete(arr(writeResponse)).build())
                .publishOn(Schedulers.elastic())
                .subscribe(deleteTestSubscriber);
        deleteTestSubscriber.awaitDone(10, TimeUnit.SECONDS);
        deleteTestSubscriber.assertValueCount(1);
        deleteTestSubscriber.assertNoErrors();
        deleteTestSubscriber.assertComplete();

        TestSubscriber<TypedReadResponse<Data>> readTestSubscriber = new TestSubscriber<>();
        svc.dataWindow(
                Req.read("test", "delete",
                        writeResponse.getWriteKey())
                        .build())
                .map(dataWindowTransformer)
                .publishOn(Schedulers.elastic())
                .subscribe(readTestSubscriber);
        readTestSubscriber.awaitDone(10, TimeUnit.SECONDS);
        readTestSubscriber.assertValueCount(0);
        readTestSubscriber.assertNoErrors();
        readTestSubscriber.assertComplete();
    }

    @Test
    public void deleteMissing() throws Exception {
        FirebaseService svc = client.request();
        TestSubscriber<DeleteResponse> deleteTestSubscriber = new TestSubscriber<>();
        svc.delete(Req.delete("test", "delete", "missing").build())
                .publishOn(Schedulers.elastic())
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
