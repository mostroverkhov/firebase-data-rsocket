package com.github.mostroverkhov.firebase_rsocket.api;

import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.notifications.NotifResponse;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.notifications.TypedNotifResponse;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.read.ReadResponse;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.read.TypedReadResponse;
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
