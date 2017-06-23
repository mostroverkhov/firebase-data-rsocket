package com.github.mostroverkhov.firebase_rsocket.internal.handler.read;

import com.github.mostroverkhov.datawindowsource.model.DataQuery;
import com.github.mostroverkhov.firebase_data_rxjava.rx.FirebaseDatabaseManager;
import com.github.mostroverkhov.firebase_data_rxjava.rx.model.Window;
import com.github.mostroverkhov.firebase_rsocket.servercommon.KeyValue;
import com.github.mostroverkhov.firebase_rsocket.servercommon.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket.servercommon.model.read.TypedReadResponse;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.read.cache.firebase.CacheDuration;
import com.google.firebase.database.DatabaseReference;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.Flowable;
import rx.Observable;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DataWindowHandler extends BaseDataWindowHandler<TypedReadResponse<?>> {

    public DataWindowHandler(String key, String value) {
        super(key, value);
    }

    @Override
    public Flowable<TypedReadResponse<?>> handle(KeyValue metadata, ReadRequest readRequest) {
        DataQuery dataQuery = toDataQuery(readRequest);
        DatabaseReference dbRef = dataQuery.getDbRef();

        tryCache(readRequest, dbRef);

        Observable<Window<Object>> windowStream =
                new FirebaseDatabaseManager(dbRef)
                        .data()
                        .window(dataQuery);
        Flowable<Window<Object>> windowFlow = RxJavaInterop
                .toV2Flowable(windowStream);
        Flowable<TypedReadResponse<?>> payloadFlow = windowFlow
                .map(window -> readResponse(readRequest, window));

        return payloadFlow;

    }

    private <T> TypedReadResponse<T> readResponse(ReadRequest readRequest,
                                                  Window<T> window) {
        return new TypedReadResponse<>(
                nextReadRequest(readRequest,
                        window.getDataQuery()),
                window.dataWindow());
    }

    @SuppressWarnings("Duplicates")
    private void tryCache(ReadRequest readRequest, DatabaseReference dbRef) {
        cache.ifPresent(c -> {
            CacheDuration dur = c.cacheDuration();
            dur.readRequest(readRequest);
            long duration = dur.getDuration();
            c.nativeCache().cache(dbRef, duration, TimeUnit.SECONDS);
        });
    }
}
