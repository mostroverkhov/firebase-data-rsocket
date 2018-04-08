package com.github.mostroverkhov.firebase_rsocket.internal.handler.read;

import com.github.mostroverkhov.datawindowsource.model.DataQuery;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.RequestHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.read.cache.Cache;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.read.cache.CacheDuration;
import com.github.mostroverkhov.firebase_rsocket.model.Path;
import com.github.mostroverkhov.firebase_rsocket.model.read.ReadRequest;
import com.google.firebase.database.DatabaseReference;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/** Created with IntelliJ IDEA. Author: mostroverkhov */
abstract class ReadHandler extends RequestHandler {

  private final Optional<Cache> cache;

  public ReadHandler(Optional<Cache> cache) {
    this.cache = cache;
  }

  Optional<Cache> getCache() {
    return cache;
  }

  DataQuery asDataQuery(ReadRequest readRequest) {
    Path path = readRequest.getPath();
    DatabaseReference dataRef = reference(path);

    DataQuery.Builder builder = new DataQuery.Builder(dataRef);
    builder.windowWithSize(readRequest.getWindowSize());
    if (readRequest.isAsc()) {
      builder.asc();
    } else {
      builder.desc();
    }
    String windowStartWith = readRequest.getWindowStartWith();
    if (windowStartWith != null) {
      builder.startWith(windowStartWith);
    }
    ReadRequest.OrderBy orderBy = readRequest.getOrderBy();
    if (orderBy == ReadRequest.OrderBy.KEY) {
      builder.orderByKey();
    } else if (orderBy == ReadRequest.OrderBy.VALUE) {
      builder.orderByValue();
    } else if (orderBy == ReadRequest.OrderBy.CHILD && readRequest.getOrderByChildKey() != null) {
      builder.orderByChild(readRequest.getOrderByChildKey());
    } else throw new IllegalStateException("Wrong order by: " + readRequest);

    return builder.build();
  }

  ReadRequest nextReadRequest(ReadRequest readRequest, DataQuery dataQuery) {
    return new ReadRequest(
        readRequest.getPath(),
        readRequest.getWindowSize(),
        readRequest.getOrderDir(),
        readRequest.getOrderBy(),
        readRequest.getOrderByChildKey(),
        dataQuery.getWindowStartWith());
  }

  void cache(ReadRequest readRequest, DatabaseReference dbRef) {
    getCache()
        .ifPresent(
            c -> {
              CacheDuration dur = c.cacheDuration();
              long duration = dur.getDurationSeconds(readRequest);
              c.nativeCache().cache(dbRef, duration, TimeUnit.SECONDS);
            });
  }
}
