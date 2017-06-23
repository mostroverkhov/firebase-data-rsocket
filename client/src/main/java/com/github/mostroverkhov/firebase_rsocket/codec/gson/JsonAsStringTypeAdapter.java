package com.github.mostroverkhov.firebase_rsocket.codec.gson;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class JsonAsStringTypeAdapter extends TypeAdapter<String> {
    private final Gson gson;
    private final TypeAdapter<String> delegateAdapter;

    public JsonAsStringTypeAdapter(Gson gson, TypeAdapter<String> delegateAdapter) {
        this.gson = gson;
        this.delegateAdapter = delegateAdapter;
    }

    @Override
    public String read(JsonReader in) throws IOException {

        JsonToken peek = in.peek();

        if (peek == JsonToken.BEGIN_OBJECT) {
            TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);
            JsonObject read = adapter.read(in);
            return read.toString();
        }

        if (peek == JsonToken.BEGIN_ARRAY) {
            TypeAdapter<JsonArray> adapter = gson.getAdapter(JsonArray.class);
            JsonArray read = adapter.read(in);
            return read.toString();
        }
        if (delegateAdapter != null) {
            return delegateAdapter.read(in);
        } else {
            return null;
        }
    }

    @Override
    public void write(JsonWriter out, String value) throws IOException {
        out.value(value);
    }

    public static class JsonAsStringTypeAdapterFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (String.class.isAssignableFrom(type.getRawType())) {
                TypeToken<String> typeToken = new TypeToken<String>() {
                };
                TypeAdapter<String> delegate = gson.getDelegateAdapter(this, typeToken);
                return (TypeAdapter<T>) new JsonAsStringTypeAdapter(gson, delegate);
            }
            return null;
        }
    }
}
