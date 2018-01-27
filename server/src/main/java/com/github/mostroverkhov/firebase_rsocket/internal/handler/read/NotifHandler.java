package com.github.mostroverkhov.firebase_rsocket.internal.handler.read;

import com.github.mostroverkhov.datawindowsource.model.DataItem;
import com.github.mostroverkhov.datawindowsource.model.DataQuery;
import com.github.mostroverkhov.datawindowsource.model.NextQuery;
import com.github.mostroverkhov.datawindowsource.model.WindowChangeEvent;
import com.github.mostroverkhov.firebase_data_rxjava.rx.FirebaseDatabaseManager;
import com.github.mostroverkhov.firebase_rsocket.model.notifications.NotifEventKind;
import com.github.mostroverkhov.firebase_rsocket.model.notifications.TypedNotifResponse;
import com.github.mostroverkhov.firebase_rsocket.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.read.cache.Cache;
import com.google.firebase.database.DatabaseReference;
import reactor.core.publisher.Flux;
import rx.Observable;

import java.util.Optional;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class NotifHandler extends ReadHandler {

    public NotifHandler(Optional<Cache> cache) {
        super(cache);
    }

    public Flux<TypedNotifResponse> handle(ReadRequest request) {
        return Flux.defer(() -> {
            DataQuery dataQuery = asDataQuery(request);
            DatabaseReference dbRef = dataQuery.getDbRef();
            cache(request, dbRef);

            Observable<DataItem> notifications = new FirebaseDatabaseManager(dbRef)
                    .data()
                    .notifications(dataQuery);

            return asFlux(notifications)
                    .map(dataItem -> notification(request, dataItem));
        });
    }

    private TypedNotifResponse notification(ReadRequest curRequest,
                                            DataItem dataItem) {

        if (dataItem instanceof NextQuery) {
            NextQuery nextQuery = (NextQuery) dataItem;
            ReadRequest nextReadRequest = nextReadRequest(
                    curRequest,
                    nextQuery.getNext());
            return TypedNotifResponse.nextWindow(nextReadRequest);

        } else if (dataItem instanceof WindowChangeEvent) {
            WindowChangeEvent changeEvent = (WindowChangeEvent) dataItem;
            NotifEventKind eventKind = kind(changeEvent.getKind());
            return TypedNotifResponse.changeEvent(
                    eventKind,
                    changeEvent.getItem());
        } else {
            throw unknownType(dataItem);
        }
    }

    private NotifEventKind kind(WindowChangeEvent.Kind kind) {
        switch (kind) {
            case ADDED:
                return NotifEventKind.ADDED;
            case MOVED:
                return NotifEventKind.MOVED;
            case CHANGED:
                return NotifEventKind.CHANGED;
            case REMOVED:
                return NotifEventKind.REMOVED;
            default:
                throw new AssertionError("Unknown notification event kind: " + kind);
        }
    }

    private IllegalArgumentException unknownType(DataItem dataItem) {
        return new IllegalArgumentException("Unknown data window notification type: "
                + dataItem.getClass().getName());
    }
}
