package com.github.mostroverkhov.firebase_rsocket.internal.mapper;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.DataCodec;
import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;

import java.util.Optional;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public class MetadataMapper<T> implements ServerMapper<T> {
    private volatile DataCodec dataCodec;
    private final Class<T> targetType;
    private final String key;
    private final String[] ops;

    public MetadataMapper(Class<T> targetType,
                          String key,
                          String... values) {
        this.targetType = targetType;
        this.key = key;
        this.ops = values;
    }

    @Override
    public boolean accepts(KeyValue metaData) {
        for (String op : ops) {
            if (metaData.contains(key, op)) {
                return true;
            }
        }
        return false;
    }

    public MetadataMapper<T> setDataCodec(DataCodec dataCodec) {
        this.dataCodec = dataCodec;
        return this;
    }

    @Override
    public Optional<T> map(KeyValue metadata, byte[] data) {
        assertCodec();
        T val = dataCodec.decode(data, targetType);
        if (val == null) {
            throw mapDataError(data);
        }
        return Optional.of(val);
    }

    @Override
    public byte[] marshall(Object response) {
        return dataCodec.encode(response);
    }

    private String bytesToMessage(byte[] data) {
        Optional<String> maybeData = dataCodec.decode(data);
        return maybeData.orElse("(non_processable)");
    }

    private void assertCodec() {
        if (dataCodec == null) {
            throw new IllegalStateException("DataCodec was not set");
        }
    }

    private IllegalArgumentException mapDataError(byte[] data) {
        return new IllegalArgumentException("Error mapping  payload data: " + bytesToMessage(data));
    }
}