package com.github.mostroverkhov.firebase_rsocket.internal.handler.impl.read;

import com.github.mostroverkhov.datawindowsource.model.DataItem;
import com.github.mostroverkhov.datawindowsource.model.DataQuery;
import com.github.mostroverkhov.datawindowsource.model.NextQuery;
import com.github.mostroverkhov.datawindowsource.model.WindowChangeEvent;
import com.github.mostroverkhov.firebase_data_rxjava.rx.FirebaseDatabaseManager;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.impl.read.cache.firebase.Cache;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.impl.read.cache.firebase.CacheDuration;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.DataWindowChangeEvent;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.DataWindowNotif;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NextWindow;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
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
public class NotifRequestHandler extends BaseDataWindowHandler<DataWindowNotif> {

    private final Optional<Cache> cache;

    public NotifRequestHandler(Cache cache) {
        super(Op.DATA_WINDOW_NOTIF);
        this.cache = Optional.of(cache);
    }

    public NotifRequestHandler() {
        super(Op.DATA_WINDOW_NOTIF);
        this.cache = Optional.empty();
    }

    @Override
    public Flowable<DataWindowNotif> handle(ReadRequest request) {
        DataQuery dataQuery = toDataQuery(request);
        DatabaseReference dbRef = dataQuery.getDbRef();

        tryCache(request, dbRef);

        Observable<DataItem> notifications = new FirebaseDatabaseManager(dbRef)
                .data()
                .notifications(dataQuery);
        Flowable<DataWindowNotif> dataWindowNotifFlow =
                RxJavaInterop.toV2Flowable(notifications)
                        .map(dataItem -> toNotif(request, dataItem));
        return dataWindowNotifFlow;
    }

    private DataWindowNotif toNotif(ReadRequest curRequest, DataItem dataItem) {
        if (dataItem instanceof NextQuery) {
            NextQuery nextQuery = (NextQuery) dataItem;
            ReadRequest nextReadRequest = nextReadRequest(curRequest, nextQuery.getNext());
            return new NextWindow(nextReadRequest);
        } else if (dataItem instanceof WindowChangeEvent) {
            WindowChangeEvent changeEvent = (WindowChangeEvent) dataItem;
            return new DataWindowChangeEvent<>(changeEvent.getItem(), toKind(changeEvent.getKind()));
        } else {
            throw unknownType(dataItem);
        }
    }

    private DataWindowChangeEvent.EventKind toKind(WindowChangeEvent.Kind kind) {
        switch (kind) {
            case ADDED:
                return DataWindowChangeEvent.EventKind.ADDED;
            case MOVED:
                return DataWindowChangeEvent.EventKind.MOVED;
            case CHANGED:
                return DataWindowChangeEvent.EventKind.CHANGED;
            case REMOVED:
                return DataWindowChangeEvent.EventKind.REMOVED;
            default:
                throw new AssertionError("Unknown notification event kind: " + kind);
        }
    }

    private IllegalArgumentException unknownType(DataItem dataItem) {
        return new IllegalArgumentException("Unknown data window notification type: "
                + dataItem.getClass().getName());
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
