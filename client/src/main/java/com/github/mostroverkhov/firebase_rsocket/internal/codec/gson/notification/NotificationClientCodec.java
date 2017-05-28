package com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.notification;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.Serializer;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.GsonClientCodec;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NonTypedNotificationResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class NotificationClientCodec extends GsonClientCodec<ReadRequest, NonTypedNotificationResponse> {

    public NotificationClientCodec(Serializer serializer) {
        super(NonTypedNotificationResponse.class, serializer);
    }
}
