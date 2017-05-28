package com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.notification;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NonTypedNotifResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NotifEventKind;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NotifResponse;
import com.google.gson.Gson;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class NotificationTransformer<T> implements Function<
        NonTypedNotifResponse,
        Flowable<NotifResponse<T>>> {

    private Gson gson;
    private Class<T> itemType;

    public NotificationTransformer(Gson gson, Class<T> itemType) {
        this.gson = gson;
        this.itemType = itemType;
    }

    @Override
    public Flowable<NotifResponse<T>> apply(NonTypedNotifResponse input) throws Exception {
        return Flowable.just(typedResponse(input));
    }

    private NotifResponse<T> typedResponse(NonTypedNotifResponse input) {
        if (input.isNextWindow()) {
            return NotifResponse.nextWindow(
                    input.getNextDataWindow());
        } else {
            NotifEventKind kind = input.getKind();
            T item = typedItem(input.getItem());
            return NotifResponse.changeEvent(kind, item);
        }
    }

    private T typedItem(String item) {
        return gson.fromJson(item, itemType);
    }
}
