package com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.notification;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.util.GsonSerializer;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.EventKind;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NonTypedNotificationResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NotificationResponse;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class NotificationTransformer<T> implements Function<
        NonTypedNotificationResponse,
        Flowable<NotificationResponse<T>>> {

    private final GsonSerializer serializer;
    private Class<T> itemType;

    public NotificationTransformer(GsonSerializer serializer, Class<T> itemType) {
        this.serializer = serializer;
        this.itemType = itemType;
    }

    @Override
    public Flowable<NotificationResponse<T>> apply(NonTypedNotificationResponse input) throws Exception {
        return Flowable.just(typedResponse(input));
    }

    private NotificationResponse<T> typedResponse(NonTypedNotificationResponse input) {
        if (input.isNextWindow()) {
            return NotificationResponse.nextWindow(
                    input.getNextDataWindow());
        } else {
            EventKind kind = input.getKind();
            T item = typedItem(input.getItem());
            return NotificationResponse.changeEvent(kind, item);
        }
    }

    private T typedItem(String item) {
        return serializer.getGson().fromJson(item, itemType);
    }
}
