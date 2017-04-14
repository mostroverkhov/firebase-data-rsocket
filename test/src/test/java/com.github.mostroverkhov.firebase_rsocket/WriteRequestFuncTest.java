package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteResponse;
import com.google.firebase.database.*;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class WriteRequestFuncTest extends AbstractTest {

    private DatabaseReference writtenRef;

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        cleanUpWrittenValueNative();
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

    private void cleanUpWrittenValueNative() throws InterruptedException {
        if (writtenRef != null) {
            CountDownLatch nativeRemoveLatch = new CountDownLatch(1);
            writtenRef.removeValue((databaseError, databaseReference) -> nativeRemoveLatch.countDown());
            nativeRemoveLatch.await(10, TimeUnit.SECONDS);
            writtenRef = null;
        }
    }
}
