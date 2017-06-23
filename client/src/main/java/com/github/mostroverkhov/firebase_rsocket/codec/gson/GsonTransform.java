package com.github.mostroverkhov.firebase_rsocket.codec.gson;

import com.github.mostroverkhov.firebase_rsocket.api.Transform;
import com.github.mostroverkhov.firebase_rsocket.api.Transformer;
import com.github.mostroverkhov.firebase_rsocket.codec.gson.transformers.notification.NotificationTransformer;
import com.github.mostroverkhov.firebase_rsocket.codec.gson.transformers.read.DataWindowTransformer;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.notifications.NotifResponse;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.notifications.TypedNotifResponse;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.read.ReadResponse;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.read.TypedReadResponse;
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
