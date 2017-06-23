package com.github.mostroverkhov.firebase_rsocket.api;

import com.github.mostroverkhov.firebase_rsocket.api.gson.transformers.Transformer;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NotifResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.TypedNotifResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.TypedReadResponse;
import io.reactivex.Flowable;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface Transform {
    <T> Transformer<
            ReadResponse,
            Flowable<TypedReadResponse<T>>> dataWindowOf(Class<T> windowItemsType);

    <T> Transformer<
            NotifResponse,
                    Flowable<TypedNotifResponse<T>>> notificationsOf(Class<T> itemType);
}
