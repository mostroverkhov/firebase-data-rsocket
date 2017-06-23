package com.github.mostroverkhov.firebase_rsocket.internal.handler.read;

import com.github.mostroverkhov.datawindowsource.model.DataQuery;
import com.github.mostroverkhov.firebase_rsocket.servercommon.model.Path;
import com.github.mostroverkhov.firebase_rsocket.servercommon.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.MetadataRequestHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.read.cache.firebase.Cache;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.read.cache.firebase.HasCache;
import com.google.firebase.database.DatabaseReference;

import java.util.Optional;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public abstract class BaseDataWindowHandler<Resp>
        extends MetadataRequestHandler<ReadRequest, Resp>
        implements HasCache {

    protected volatile Optional<Cache> cache = Optional.empty();

    public BaseDataWindowHandler(String key, String value) {
        super(key, value);
    }

    @Override
    public BaseDataWindowHandler<Resp> setCache(Cache cache) {
        this.cache = Optional.of(cache);
        return this;
    }

    protected Optional<Cache> getCache() {
        return cache;
    }

    protected DataQuery toDataQuery(ReadRequest readRequest) {

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
        } else if (orderBy == ReadRequest.OrderBy.CHILD
                && readRequest.getOrderByChildKey() != null) {
            builder.orderByChild(readRequest.getOrderByChildKey());
        } else throw new IllegalStateException("Wrong order by: " + readRequest);

        return builder.build();
    }

    static ReadRequest nextReadRequest(ReadRequest readRequest,
                                       DataQuery dataQuery) {
        return new ReadRequest(
                readRequest.getPath(),
                readRequest.getWindowSize(),
                readRequest.getOrderDir(),
                readRequest.getOrderBy(),
                readRequest.getOrderByChildKey(),
                dataQuery.getWindowStartWith()
        );
    }
}
