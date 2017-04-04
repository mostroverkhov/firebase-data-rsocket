package com.github.mostroverkhov.firebase_rsocket.internal.mapper;

import com.github.mostroverkhov.firebase_rsocket_data.common.Conversions;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public class OperationBasedRequestMapper<T extends Operation> implements ServerRequestMapper<T> {

    private final Gson gson;
    private final Class<T> targetType;
    private final Set<String> ops = new HashSet<>();

    public OperationBasedRequestMapper(Gson gson,
                                       Class<T> targetType,
                                       String... ops) {
        this.gson = gson;
        this.targetType = targetType;
        this.ops.addAll(Arrays.asList(ops));
    }

    @Override
    public Optional<T> map(byte[] request) {
        BufferedReader reader = Conversions.bytesToReader(request);
        TypeAdapter<JsonElement> adapter = gson.getAdapter(JsonElement.class);
        JsonElement element = jsonElement(reader, adapter);
        String op = element
                .getAsJsonObject()
                .get("operation").getAsString();
        if (ops.contains(op)) {
            return Optional.of(gson.fromJson(element, targetType));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public byte[] marshall(Object response) {
        String responseStr = gson.toJson(response);
        return Conversions.stringToBytes(responseStr);
    }

    private JsonElement jsonElement(Reader reader,
                                    TypeAdapter<JsonElement> adapter) {
        JsonElement element;
        try {
            element = adapter.read(new JsonReader(reader));
        } catch (IOException e) {
            throw new RuntimeException("Error while reading request json", e);
        }
        return element;
    }
}