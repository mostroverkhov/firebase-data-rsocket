package com.github.mostroverkhov.firebase_rsocket.internal.codec.gson;

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

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;

import static com.github.mostroverkhov.firebase_rsocket_data.common.Conversions.bytesToReader;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class NotificationClientCodec<T> extends GsonClientCodec<ReadRequest, NotifResponse> {

    private final Class<T> notifItemType;

    public NotificationClientCodec(Class<T> notifItemType) {
        this.notifItemType = notifItemType;
    }

    @Override
    public NotifResponse map(byte[] response) {
        return mapResponse(serializer(), response);
    }

    private NotifResponse mapResponse(GsonSerializer gsonSerializer,
                                      byte[] payload) {

        Gson gson = gsonSerializer.getGson();
        Charset charset = Charset.forName(gsonSerializer.getEncoding());

        Reader reader = bytesToReader(payload, charset);

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
