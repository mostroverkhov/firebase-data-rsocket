package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.model.delete.DeleteResponse;
import com.github.mostroverkhov.firebase_rsocket.model.read.TypedReadResponse;
import com.github.mostroverkhov.firebase_rsocket.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket.model.write.WriteResponse;
import com.github.mostroverkhov.firebase_rsocket.requests.Req;
import com.google.firebase.database.FirebaseDatabase;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DeleteRequestFuncTest extends AbstractTest {

    private WriteResponse writeResponse;

    @Test
    public void deletePresent() {

        Data data = new Data("w", "w");
        WriteRequest<Data> writeRequest = Req
            .<Data>write("test", "delete")
            .data(data)
            .build();

        FirebaseService svc = client.request();
        writeResponse = svc
            .write(writeRequest)
            .subscribeOn(Schedulers.elastic()).blockFirst();
        if (writeResponse == null) {
            throw new IllegalStateException(
                "Write request returned no response " + writeRequest);
        }

        Flux<DeleteResponse> deleteResponse = svc
            .delete(Req.delete(arr(writeResponse)).build())
            .publishOn(Schedulers.elastic());
        StepVerifier.create(deleteResponse)
            .expectNextCount(1)
            .expectComplete().verify(Duration.ofSeconds(10));

        Flux<TypedReadResponse<Data>> readResponse = svc
            .dataWindow(
                Req.read("test", "delete",
                    writeResponse.getWriteKey()).build())
            .map(dataWindowTransformer)
            .publishOn(Schedulers.elastic());
        StepVerifier.create(readResponse)
            .expectComplete()
            .verify(Duration.ofSeconds(10));
    }

    @Test
    public void deleteMissing() {
        FirebaseService svc = client.request();
        Flux<DeleteResponse> deleteResponse = svc
            .delete(Req.delete("test", "delete", "missing").build())
            .publishOn(Schedulers.elastic());

        StepVerifier.create(deleteResponse)
            .expectNextCount(1)
            .expectComplete()
            .verify(Duration.ofSeconds(10));
    }

    @Override
    public void tearDown() throws Exception {
        if (writeResponse != null) {
            String writeKey = writeResponse.getWriteKey();
            CountDownLatch latch = new CountDownLatch(1);
            FirebaseDatabase.getInstance().getReference().child("test").child("delete").child(writeKey)
                    .removeValue((databaseError, databaseReference) -> latch.countDown());
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
