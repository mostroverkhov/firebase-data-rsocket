package com.github.mostroverkhov.firebase_rsocket.typed;

import com.github.mostroverkhov.firebase_rsocket.model.notifications.NotifResponse;
import com.github.mostroverkhov.firebase_rsocket.model.notifications.TypedNotifResponse;
import com.github.mostroverkhov.firebase_rsocket.model.read.ReadResponse;
import com.github.mostroverkhov.firebase_rsocket.model.read.TypedReadResponse;

import java.util.function.Function;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface Typed {
    <T> Function<
            ReadResponse,
            TypedReadResponse<T>> dataWindowOf(Class<T> windowItemsType);

    <T> Function<
            NotifResponse,
            TypedNotifResponse<T>> notificationsOf(Class<T> itemType);
}
