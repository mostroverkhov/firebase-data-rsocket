package com.github.mostroverkhov.firebase_rsocket.internal.handler.impl.read;

import com.github.mostroverkhov.datawindowsource.model.DataItem;
import com.github.mostroverkhov.datawindowsource.model.DataQuery;
import com.github.mostroverkhov.datawindowsource.model.NextQuery;
import com.github.mostroverkhov.datawindowsource.model.WindowChangeEvent;
import com.github.mostroverkhov.firebase_data_rxjava.rx.FirebaseDatabaseManager;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.impl.read.cache.firebase.CacheDuration;
import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.EventKind;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NonTypedNotificationResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NotificationResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.google.firebase.database.DatabaseReference;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.Flowable;
import rx.Observable;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class NotifHandler extends BaseDataWindowHandler<NotificationResponse> {

    public NotifHandler(String key, String value) {
        super(key, value);
    }

    @Override
    public Flowable<NotificationResponse> handle(KeyValue metadata, ReadRequest request) {
        DataQuery dataQuery = toDataQuery(request);
        DatabaseReference dbRef = dataQuery.getDbRef();

        tryCache(request, dbRef);

        Observable<DataItem> notifications = new FirebaseDatabaseManager(dbRef)
                .data()
                .notifications(dataQuery);
        Flowable<NotificationResponse> dataWindowNotifFlow =
                RxJavaInterop.toV2Flowable(notifications)
                        .map(dataItem -> toNotif(request, dataItem));
        return dataWindowNotifFlow;
    }

    private NotificationResponse toNotif(ReadRequest curRequest, DataItem dataItem) {
        if (dataItem instanceof NextQuery) {
            NextQuery nextQuery = (NextQuery) dataItem;
            ReadRequest nextReadRequest = nextReadRequest(curRequest, nextQuery.getNext());
            return NotificationResponse.nextWindow(nextReadRequest);
        } else if (dataItem instanceof WindowChangeEvent) {
            WindowChangeEvent changeEvent = (WindowChangeEvent) dataItem;
            EventKind eventKind = toKind(changeEvent.getKind());
            return NotificationResponse.changeEvent(eventKind, changeEvent.getItem());
        } else {
            throw unknownType(dataItem);
        }
    }

    private EventKind toKind(WindowChangeEvent.Kind kind) {
        switch (kind) {
            case ADDED:
                return EventKind.ADDED;
            case MOVED:
                return EventKind.MOVED;
            case CHANGED:
                return EventKind.CHANGED;
            case REMOVED:
                return EventKind.REMOVED;
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
