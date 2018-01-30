
package com.github.mostroverkhov.firebase_rsocket.typed.gson.converter;

import com.github.mostroverkhov.firebase_rsocket.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket.model.read.ReadResponse;
import com.github.mostroverkhov.firebase_rsocket.model.read.TypedReadResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */

public class DataWindowConverter<T> implements Function<ReadResponse, TypedReadResponse<T>> {
    private final Gson gson;
    private final Class<T> itemsType;

    public DataWindowConverter(Gson gson, Class<T> itemsType) {
        this.gson = gson;
        this.itemsType = itemsType;
    }

    @Override
    public TypedReadResponse<T> apply(ReadResponse nonTyped) {
        return typedWindow(nonTyped);
    }

    private TypedReadResponse<T> typedWindow(ReadResponse nonTyped) {
        ReadRequest readRequest = nonTyped.getReadRequest();
        String data = nonTyped.getData();
        List<T> typedData = typedItems(data);
        return new TypedReadResponse<>(readRequest, typedData);
    }

    private List<T> typedItems(String items) {
        return typedItems(gson, itemsType, asArray(gson, items));
    }

    private static <T> List<T> typedItems(Gson gson,
                                          Class<T> itemType,
                                          JsonArray jsonArray) {
        List<T> data = new ArrayList<>();
        for (JsonElement dataItemJson : jsonArray) {
            T t = gson.fromJson(dataItemJson, itemType);
            data.add(t);
        }
        return data;
    }

    private static JsonArray asArray(Gson gson, String items) {
        TypeAdapter<JsonElement> adapter = gson.getAdapter(JsonElement.class);
        Reader reader = new BufferedReader(new StringReader(items));
        try {
            return adapter.fromJson(reader).getAsJsonArray();
        } catch (IOException e) {
            throw new RuntimeException("Error while reading json input ", e);
        }
    }
}

