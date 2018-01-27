package com.github.mostroverkhov.firebase_rsocket.typed.gson;

import com.github.mostroverkhov.firebase_rsocket.model.notifications.NotifResponse;
import com.github.mostroverkhov.firebase_rsocket.model.notifications.TypedNotifResponse;
import com.github.mostroverkhov.firebase_rsocket.model.read.ReadResponse;
import com.github.mostroverkhov.firebase_rsocket.model.read.TypedReadResponse;
import com.github.mostroverkhov.firebase_rsocket.typed.Typed;
import com.github.mostroverkhov.firebase_rsocket.typed.gson.converter.DataWindowConverter;
import com.github.mostroverkhov.firebase_rsocket.typed.gson.converter.NotificationConverter;
import com.google.gson.Gson;

import java.util.function.Function;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */

public class GsonTyped implements Typed {

    private final Gson gson;

    public GsonTyped(Gson gson) {
        this.gson = gson;
    }

    @Override
    public <T> Function<
            ReadResponse,
            TypedReadResponse<T>> dataWindowOf(Class<T> windowItemsType) {
        return new DataWindowConverter<>(gson, windowItemsType);
    }

    @Override
    public <T> Function<
            NotifResponse,
            TypedNotifResponse<T>> notificationsOf(Class<T> itemType) {
        return new NotificationConverter<>(gson, itemType);
    }
}

