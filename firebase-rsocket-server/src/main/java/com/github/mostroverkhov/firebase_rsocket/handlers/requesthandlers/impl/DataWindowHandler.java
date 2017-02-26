package com.github.mostroverkhov.firebase_rsocket.handlers.requesthandlers.impl;

import com.github.mostroverkhov.datawindowsource.model.DataQuery;
import com.github.mostroverkhov.firebase_data_rxjava.rx.FirebaseDatabaseManager;
import com.github.mostroverkhov.firebase_data_rxjava.rx.model.Window;
import com.github.mostroverkhov.firebase_rsocket.ServerSocketAcceptor;
import com.github.mostroverkhov.firebase_rsocket.handlers.cache.firebase.CacheDuration;
import com.github.mostroverkhov.firebase_rsocket.handlers.cache.firebase.NativeCache;
import com.github.mostroverkhov.firebase_rsocket.handlers.requesthandlers.RequestHandler;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Path;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadResponse;
import com.google.firebase.database.DatabaseReference;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivesocket.Payload;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import rx.Observable;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.github.mostroverkhov.firebase_rsocket.handlers.requesthandlers.impl.HandlerCommon.payload;
import static com.github.mostroverkhov.firebase_rsocket.handlers.requesthandlers.impl.HandlerCommon.reference;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DataWindowHandler implements RequestHandler {

    private final Optional<Cache> cache;

    public DataWindowHandler() {
        this.cache = Optional.empty();
    }

    public DataWindowHandler(Cache cache) {
        this.cache = Optional.of(cache);
    }

    @Override
    public boolean canHandle(Operation op) {
        return Op.DATA_WINDOW.code().equals(op.getOp());
    }

    @Override
    public Publisher<Payload> handle(ServerSocketAcceptor.SocketContext context, Operation op) {

        ReadRequest readRequest = (ReadRequest) op;
        DataQuery dataQuery = toDataQuery(readRequest);
        DatabaseReference dbRef = dataQuery.getDbRef();

        tryCache(readRequest, dbRef);

        Observable<Window<Object>> windowStream =
                new FirebaseDatabaseManager(dbRef)
                        .data()
                        .window(dataQuery);
        Flowable<Window<Object>> windowFlow = RxJavaInterop
                .toV2Flowable(windowStream);
        Flowable<Payload> payloadFlow = windowFlow
                .map(window -> new ReadResponse<>(
                        readRequest,
                        window.dataWindow()))
                .map(dw -> payload(context.gson(), dw));

        return payloadFlow;

    }

    private void tryCache(ReadRequest readRequest, DatabaseReference dbRef) {
        cache.ifPresent(c -> {
            CacheDuration dur = c.cacheDuration();
            dur.readRequest(readRequest);
            long duration = dur.getDuration();
            c.nativeCache().cache(dbRef, duration, TimeUnit.SECONDS);
        });
    }

    private static DataQuery toDataQuery(ReadRequest readRequest) {

        Path path = readRequest.getPath();
        DatabaseReference dataRef = reference(path);

        DataQuery.Builder builder = new DataQuery.Builder(dataRef);
        builder.windowWithSize(readRequest.getWindowSize());
        if (readRequest.isAsc()) {
            builder.asc();
        } else {
            builder.desc();
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

    public static class Cache {
        private final NativeCache nativeCache;
        private final CacheDuration cacheDuration;

        public Cache(NativeCache nativeCache,
                     CacheDuration cacheDuration) {
            this.nativeCache = nativeCache;
            this.cacheDuration = cacheDuration;
        }

        public NativeCache nativeCache() {
            return nativeCache;
        }

        public CacheDuration cacheDuration() {
            return cacheDuration;
        }
    }
}
