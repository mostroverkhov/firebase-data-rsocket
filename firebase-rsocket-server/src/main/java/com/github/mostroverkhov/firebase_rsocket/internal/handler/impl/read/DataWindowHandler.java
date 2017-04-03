package com.github.mostroverkhov.firebase_rsocket.internal.handler.impl.read;

import com.github.mostroverkhov.datawindowsource.model.DataQuery;
import com.github.mostroverkhov.firebase_data_rxjava.rx.FirebaseDatabaseManager;
import com.github.mostroverkhov.firebase_data_rxjava.rx.model.Window;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.impl.read.cache.firebase.Cache;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.impl.read.cache.firebase.CacheDuration;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadResponse;
import com.google.firebase.database.DatabaseReference;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.Flowable;
import rx.Observable;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DataWindowHandler extends BaseDataWindowHandler<ReadResponse<?>> {

    private Optional<Cache> cache;

    public DataWindowHandler() {
        this(Optional.empty());
    }

    public DataWindowHandler(Cache cache) {
        this(Optional.of(cache));
    }

    private DataWindowHandler(Optional<Cache> cache) {
        super(Op.DATA_WINDOW);
        this.cache = cache;
    }

    @Override
    public Flowable<ReadResponse<?>> handle(ReadRequest readRequest) {
        DataQuery dataQuery = toDataQuery(readRequest);
        DatabaseReference dbRef = dataQuery.getDbRef();

        tryCache(readRequest, dbRef);

        Observable<Window<Object>> windowStream =
                new FirebaseDatabaseManager(dbRef)
                        .data()
                        .window(dataQuery);
        Flowable<Window<Object>> windowFlow = RxJavaInterop
                .toV2Flowable(windowStream);
        Flowable<ReadResponse<?>> payloadFlow = windowFlow
                .map(window -> readResponse(readRequest, window));

        return payloadFlow;

    }

    private <T> ReadResponse<T> readResponse(ReadRequest readRequest,
                                             Window<T> window) {
        return new ReadResponse<>(
                nextReadRequest(readRequest,
                        window.getDataQuery()),
                window.dataWindow());
    }

    private void tryCache(ReadRequest readRequest, DatabaseReference dbRef) {
        cache.ifPresent(c -> {
            CacheDuration dur = c.cacheDuration();
            dur.readRequest(readRequest);
            long duration = dur.getDuration();
            c.nativeCache().cache(dbRef, duration, TimeUnit.SECONDS);
        });
    }
}
