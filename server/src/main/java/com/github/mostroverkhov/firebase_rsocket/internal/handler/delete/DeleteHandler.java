package com.github.mostroverkhov.firebase_rsocket.internal.handler.delete;

import com.github.mostroverkhov.firebase_data_rxjava.rx.FirebaseDatabaseManager;
import com.github.mostroverkhov.firebase_data_rxjava.rx.model.WriteResult;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.RequestHandler;
import com.github.mostroverkhov.firebase_rsocket.model.Path;
import com.github.mostroverkhov.firebase_rsocket.model.delete.DeleteRequest;
import com.github.mostroverkhov.firebase_rsocket.model.delete.DeleteResponse;
import com.google.firebase.database.DatabaseReference;
import reactor.core.publisher.Flux;
import rx.Observable;

/** Created with IntelliJ IDEA. Author: mostroverkhov */
public class DeleteHandler extends RequestHandler {

  public Flux<DeleteResponse> handle(DeleteRequest deleteRequest) {
    return Flux.defer(
        () -> {
          Path path = deleteRequest.getPath();
          DatabaseReference dbRef = reference(path);
          Observable<WriteResult> delete = new FirebaseDatabaseManager(dbRef).data().removeValue();

          return asFlux(delete).map(writeResult -> deleteResponse(path));
        });
  }

  private DeleteResponse deleteResponse(Path path) {
    return new DeleteResponse(path.getChildPaths());
  }
}
