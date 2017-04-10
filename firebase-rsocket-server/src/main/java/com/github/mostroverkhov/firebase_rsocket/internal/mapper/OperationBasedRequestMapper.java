package com.github.mostroverkhov.firebase_rsocket.internal.mapper;

import com.github.mostroverkhov.firebase_rsocket_data.common.Conversions;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
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
    public boolean accepts(byte[] metaData) {
        Optional<String> op = metadataOp(metaData);
        return op.map(ops::contains).orElse(false);
    }

    @Override
    public Optional<T> map(byte[] metadata, byte[] data) {
        BufferedReader reader = Conversions.bytesToReader(data);
        T val = gson.fromJson(reader, targetType);
        if (val == null) {
            throw mapDataError(data);
        }
        return Optional.of(val);
    }

    @Override
    public byte[] marshall(Object response) {
        String responseStr = gson.toJson(response);
        return Conversions.stringToBytes(responseStr);
    }

    private Optional<String> metadataOp(byte[] metaData) {
        try (JsonReader reader = new JsonReader(Conversions.bytesToReader(metaData))) {
            reader.beginObject();
            String name = reader.nextName();
            if ("operation".equals(name)) {
                String op = reader.nextString();
                return Optional.of(op);
            }
        } catch (IOException e) {
            throw mapMetadataError(metaData);
        }
        return Optional.empty();
    }

    private static String bytesToMessage(byte[] data) {
        String s;
        try {
            s = IOUtils.toString(data, "UTF-8");
        } catch (IOException e) {
            s = "(error reading message as UTF-8 string)";
        }
        return s;
    }

    private static IllegalArgumentException mapDataError(byte[] data) {
        return new IllegalArgumentException("Error mapping data: " + bytesToMessage(data));
    }

    private static IllegalStateException mapMetadataError(byte[] metaData) {
        return new IllegalStateException("Error reading metadata op: " + bytesToMessage(metaData));
    }
}