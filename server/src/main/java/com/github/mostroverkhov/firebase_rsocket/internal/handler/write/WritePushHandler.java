package com.github.mostroverkhov.firebase_rsocket.internal.handler.write;

import com.github.mostroverkhov.firebase_data_rxjava.rx.FirebaseDatabaseManager;
import com.github.mostroverkhov.firebase_data_rxjava.rx.model.WriteResult;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.RequestHandler;
import com.github.mostroverkhov.firebase_rsocket.model.Path;
import com.github.mostroverkhov.firebase_rsocket.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket.model.write.WriteResponse;
import com.google.firebase.database.DatabaseReference;
import reactor.core.publisher.Flux;
import rx.Observable;

/** Created with IntelliJ IDEA. Author: mostroverkhov */
public class WritePushHandler extends RequestHandler {

  public Flux<WriteResponse> handle(WriteRequest<?> writeRequest) {
    return Flux.defer(
        () -> {
          Path path = writeRequest.getPath();
          DatabaseReference dbRef = reference(path);
          DatabaseReference newKeyRef = dbRef.push();

          Object data = writeRequest.getData();
          Observable<WriteResult> writeResult =
              new FirebaseDatabaseManager(newKeyRef).data().setValue(data);

          return asFlux(writeResult).map(result -> writeResponse(path, newKeyRef));
        });
  }

  private WriteResponse writeResponse(Path path, DatabaseReference newKeyRef) {
    return new WriteResponse(newKeyRef.getKey(), path.getChildPaths());
  }
}
