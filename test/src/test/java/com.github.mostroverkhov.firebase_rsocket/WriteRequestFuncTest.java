
package com.github.mostroverkhov.firebase_rsocket;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.mostroverkhov.firebase_rsocket.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket.model.write.WriteResponse;
import com.github.mostroverkhov.firebase_rsocket.requests.Req;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


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
    WriteRequest<Data> writeRequest = Req
        .<Data>write("test", "write")
        .data(data)
        .build();

    Flux<WriteResponse> writeResponse =
        client
            .request()
            .write(writeRequest)
            .publishOn(Schedulers.elastic());

    List<WriteResponse> writeResponses = writeResponse
        .takeUntilOther(Mono.delay(Duration.ofSeconds(10)))
        .collectList()
        .block();

    assertThat(writeResponses)
        .hasSize(1);
    WriteResponse writeResponseVal = writeResponses.get(0);
    assertThat(writeResponseVal.getWriteKey())
        .isNotNull();

    writtenRef = writeResponseNative(writeResponseVal);
    assertWrittenDataNative(data, writtenRef);
  }

  private DatabaseReference writeResponseNative(WriteResponse writeResponse) {
    return FirebaseDatabase.getInstance()
        .getReference()
        .child("test")
        .child("write")
        .child(writeResponse.getWriteKey());
  }

  private void assertWrittenDataNative(
      Data data,
      DatabaseReference writtenRef)
      throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<WriteResult> resultRef = new AtomicReference<>();
    writtenRef
        .addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
            Data receivedData = dataSnapshot.getValue(Data.class);
            resultRef.set(new WriteResult(receivedData, null));
            latch.countDown();
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {
            IllegalStateException err = new IllegalStateException(
                "Error while reading database",
                databaseError.toException());
            resultRef.set(new WriteResult(null, err));
            latch.countDown();
          }
        });
    latch.await(10, TimeUnit.SECONDS);
    WriteResult writeResult = resultRef.get();
    if (writeResult.isError()) {
      throw writeResult.error;
    } else {
      Data receivedData = writeResult.data;
      assertThat(receivedData).isNotNull();
      assertThat(receivedData).isEqualTo(data);
    }
  }

  private void cleanUpWrittenValueNative() throws InterruptedException {
    if (writtenRef != null) {
      CountDownLatch nativeRemoveLatch = new CountDownLatch(1);
      writtenRef.removeValue(
          (databaseError, databaseReference) ->
              nativeRemoveLatch.countDown());
      nativeRemoveLatch.await(10, TimeUnit.SECONDS);
      writtenRef = null;
    }
  }

  static class WriteResult {

    final Data data;
    final RuntimeException error;

    WriteResult(Data data, RuntimeException error) {
      this.data = data;
      this.error = error;
    }

    boolean isError() {
      return error != null;
    }
  }
}

