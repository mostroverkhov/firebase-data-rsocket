package com.github.mostroverkhov.firebase_rsocket.codec.gson.transformers.notification;

import com.github.mostroverkhov.firebase_rsocket.api.Transformer;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.notifications.NotifEventKind;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.notifications.NotifResponse;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.notifications.TypedNotifResponse;
import com.google.gson.Gson;
import io.reactivex.Flowable;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class NotificationTransformer<T> implements Transformer<
        NotifResponse,
        Flowable<TypedNotifResponse<T>>> {

    private Gson gson;
    private Class<T> itemType;

    public NotificationTransformer(Gson gson, Class<T> itemType) {
        this.gson = gson;
        this.itemType = itemType;
    }

    @Override
    public Flowable<TypedNotifResponse<T>> from(NotifResponse input) {
        return Flowable.just(typedResponse(input));
    }

    private TypedNotifResponse<T> typedResponse(NotifResponse input) {
        if (input.isNextWindow()) {
            return TypedNotifResponse.nextWindow(
                    input.getNextDataWindow());
        } else {
            NotifEventKind kind = input.getKind();
            T item = typedItem(input.getItem());
            return TypedNotifResponse.changeEvent(kind, item);
        }
    }

    private T typedItem(String item) {
        return gson.fromJson(item, itemType);
    }
}
