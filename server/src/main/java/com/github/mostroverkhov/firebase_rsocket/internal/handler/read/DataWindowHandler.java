package com.github.mostroverkhov.firebase_rsocket.internal.handler.read;

import com.github.mostroverkhov.datawindowsource.model.DataQuery;
import com.github.mostroverkhov.firebase_data_rxjava.rx.FirebaseDatabaseManager;
import com.github.mostroverkhov.firebase_data_rxjava.rx.model.Window;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.read.cache.Cache;
import com.github.mostroverkhov.firebase_rsocket.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket.model.read.TypedReadResponse;
import com.google.firebase.database.DatabaseReference;
import java.util.Optional;
import reactor.core.publisher.Flux;
import rx.Observable;

/** Created with IntelliJ IDEA. Author: mostroverkhov */
public class DataWindowHandler extends ReadHandler {

  public DataWindowHandler(Optional<Cache> cache) {
    super(cache);
  }

  public Flux<TypedReadResponse<?>> handle(ReadRequest readRequest) {
    return Flux.defer(
        () -> {
          DataQuery dataQuery = asDataQuery(readRequest);
          DatabaseReference dbRef = dataQuery.getDbRef();

          cache(readRequest, dbRef);

          Observable<Window<Object>> windowStream =
              new FirebaseDatabaseManager(dbRef).data().window(dataQuery);

          return asFlux(windowStream).map(window -> readResponse(readRequest, window));
        });
  }

  private <T> TypedReadResponse<T> readResponse(ReadRequest readRequest, Window<T> window) {
    return new TypedReadResponse<>(
        nextReadRequest(readRequest, window.getDataQuery()), window.dataWindow());
  }
}
