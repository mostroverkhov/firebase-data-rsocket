package com.github.mostroverkhov.firebase_rsocket.api.gson;

import com.github.mostroverkhov.firebase_rsocket.api.Transform;
import com.github.mostroverkhov.firebase_rsocket.api.gson.transformers.Transformer;
import com.github.mostroverkhov.firebase_rsocket.api.gson.transformers.notification.NotificationTransformer;
import com.github.mostroverkhov.firebase_rsocket.api.gson.transformers.read.DataWindowTransformer;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NotifResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.TypedNotifResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.TypedReadResponse;
import com.google.gson.Gson;
import io.reactivex.Flowable;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class GsonTransform implements Transform {

    private final Gson gson;

    public GsonTransform(Gson gson) {
        this.gson = gson;
    }

    @Override
    public <T> Transformer<
            ReadResponse,
            Flowable<TypedReadResponse<T>>> dataWindowOf(Class<T> windowItemsType) {
        return new DataWindowTransformer<>(gson, windowItemsType);
    }

    @Override
    public <T> Transformer<
            NotifResponse,
            Flowable<TypedNotifResponse<T>>> notificationsOf(Class<T> itemType) {
        return new NotificationTransformer<>(gson, itemType);
    }
}
