package com.github.mostroverkhov.firebase_rsocket.gson;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.DataWindow;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Query;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maksym Ostroverkhov on 01.03.17.
 */
public class GsonUtil {

    public static <T> DataWindow<T> parseDataWindow(Gson gson,
                                                    String jsonStr,
                                                    Class<T> itemType) {
        JsonReader jsonReader = gson.newJsonReader(
                new BufferedReader(
                        new StringReader(jsonStr)));

        TypeAdapter<JsonElement> adapter = gson.getAdapter(JsonElement.class);
        JsonElement root = getRoot(jsonStr, jsonReader, adapter);

        JsonObject rootObject = root.getAsJsonObject();
        Query query = getQuery(gson, rootObject);
        List<T> data = getData(gson, itemType, rootObject);

        return new DataWindow<>(query, data);
    }

    private static Query getQuery(Gson gson,
                                  JsonObject rootObject) {
        return gson.fromJson(
                rootObject.get("query").getAsJsonObject(),
                Query.class);
    }

    private static <T> List<T> getData(Gson gson,
                                       Class<T> itemType,
                                       JsonObject rootObject) {

        JsonArray dataListJson = rootObject
                .get("data").getAsJsonArray();

        List<T> data = new ArrayList<>();
        for (JsonElement dataItemJson : dataListJson) {
            T t = gson.fromJson(dataItemJson, itemType);
            data.add(t);
        }
        return data;
    }

    private static JsonElement getRoot(String jsonStr,
                                       JsonReader jsonReader,
                                       TypeAdapter<JsonElement> adapter) {
        JsonElement root;
        try {
            root = adapter.read(jsonReader);
        } catch (IOException e) {
            throw new RuntimeException("Error while reading json: " + jsonStr);
        }
        return root;
    }
}
