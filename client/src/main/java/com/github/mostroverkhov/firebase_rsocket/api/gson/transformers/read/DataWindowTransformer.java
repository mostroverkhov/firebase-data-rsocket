package com.github.mostroverkhov.firebase_rsocket.api.gson.transformers.read;

import com.github.mostroverkhov.firebase_rsocket.api.gson.transformers.Transformer;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.TypedReadResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import io.reactivex.Flowable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DataWindowTransformer<T> implements Transformer<ReadResponse, Flowable<TypedReadResponse<T>>> {
    private final Gson gson;
    private final Class<T> itemsType;

    public DataWindowTransformer(Gson gson, Class<T> itemsType) {
        this.gson = gson;
        this.itemsType = itemsType;
    }

    @Override
    public Flowable<TypedReadResponse<T>> from(ReadResponse nonTyped) {
        return Flowable.just(typedWindow(nonTyped));
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
