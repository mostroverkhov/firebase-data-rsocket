package com.github.mostroverkhov.firebase_rsocket.internal.mapper;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.DataCodec;
import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.Optional;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public class OperationRequestMapper<T> implements ServerRequestMapper<T> {
    private final DataCodec dataCodec;
    private final Class<T> targetType;
    private final String[] ops;

    public OperationRequestMapper(DataCodec dataCodec,
                                  Class<T> targetType,
                                  String... ops) {
        this.dataCodec = dataCodec;
        this.targetType = targetType;
        this.ops = ops;
    }

    @Override
    public boolean accepts(KeyValue metaData) {
        for (String op : ops) {
            if (metaData.contains("operation", op)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<T> map(KeyValue metadata, byte[] data) {
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
}