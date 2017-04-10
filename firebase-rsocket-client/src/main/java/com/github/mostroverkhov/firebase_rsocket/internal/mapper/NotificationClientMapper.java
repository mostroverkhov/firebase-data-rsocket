package com.github.mostroverkhov.firebase_rsocket.internal.mapper;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.DataWindowChangeEvent;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NextWindow;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NotifKind;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NotifResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import java.io.IOException;
import java.io.Reader;

import static com.github.mostroverkhov.firebase_rsocket_data.common.Conversions.bytesToReader;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class NotificationClientMapper<T> extends BaseClientMapper<ReadRequest, NotifResponse> {

    private final Class<T> notifItemType;

    public NotificationClientMapper(Class<T> notifItemType) {
        this.notifItemType = notifItemType;
    }

    @Override
    public Publisher<NotifResponse> map(byte[] response) {
        return Flowable.fromCallable(() -> mapResponse(gson(), response))
                .onErrorResumeNext(mappingError("Error while mapping DataWindow notification response"));
    }

    private NotifResponse mapResponse(Gson gson,
                                      byte[] payload) {
        Reader reader = bytesToReader(payload);

        TypeAdapter<JsonElement> adapter = gson.getAdapter(JsonElement.class);
        JsonObject rootObject = getRoot(new JsonReader(reader), adapter).getAsJsonObject();

        return notifResponse(gson, rootObject);
    }

    private NotifResponse notifResponse(Gson gson,
                                        JsonObject rootObject) {

        String notifKindJ = rootObject.get("notifKind").getAsString();
        NotifKind notifKind = NotifKind.valueOf(notifKindJ);
        switch (notifKind) {

            case EVENT:
                JsonObject itemJ = rootObject.get("item").getAsJsonObject();
                String kindJ = rootObject.get("kind").getAsString();

                T t = gson.fromJson(itemJ, notifItemType);
                DataWindowChangeEvent.EventKind eventKind = DataWindowChangeEvent.EventKind.valueOf(kindJ);

                return new NotifResponse(new DataWindowChangeEvent<>(t, eventKind));

            case NEXT_WINDOW:
                NextWindow nextWindow = gson.fromJson(rootObject, NextWindow.class);
                return new NotifResponse(nextWindow);

            default:
                throw new IllegalArgumentException("Unsupported event kind: " + notifKind);
        }
    }

    private static JsonElement getRoot(JsonReader jsonReader,
                                       TypeAdapter<JsonElement> adapter) {
        JsonElement root;
        try {
            root = adapter.read(jsonReader);
        } catch (IOException e) {
            throw new RuntimeException("Error while reading json input ", e);
        }
        return root;
    }

}
