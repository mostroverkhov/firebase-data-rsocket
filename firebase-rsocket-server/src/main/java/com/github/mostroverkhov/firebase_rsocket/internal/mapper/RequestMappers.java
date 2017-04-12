package com.github.mostroverkhov.firebase_rsocket.internal.mapper;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.DataCodec;
import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public class RequestMappers implements ServerRequestMapper {
    private final List<ServerRequestMapper<?>> delegates = new ArrayList<>();

    public static RequestMappers newInstance(List<ServerRequestMapper<?>> mappers) {
        return new RequestMappers(mappers);
    }

    protected RequestMappers(ServerRequestMapper<?>... mappers) {
        this(Arrays.asList(mappers));
    }

    protected RequestMappers(List<ServerRequestMapper<?>> mappers) {
        assertAdapters(mappers);
        delegates.addAll(mappers);
    }

    @Override
    public boolean accepts(KeyValue metaData) {
        return true;
    }

    @Override
    public Optional<?> map(KeyValue metadata, byte[] data) {

        Optional<ServerRequestMapper<?>> maybeMapper = delegates
                .stream()
                .filter(mapper -> mapper.accepts(metadata))
                .findFirst();

        Optional<?> maybeOp =
                maybeMapper.flatMap(mapper -> mapper.map(metadata, data));

        return maybeOp;

    }

    @Override
    public byte[] marshall(Object response) {
        return delegates.get(0).marshall(response);
    }

    private static void assertAdapters(List<ServerRequestMapper<?>> adapters) {
        if (adapters == null) {
            throw new IllegalArgumentException("Adapters should not be null");
        }
        if (adapters.isEmpty()) {
            throw new IllegalArgumentException("Adapters should not be empty");
        }
    }

    @Override
    public RequestMappers setDataCodec(DataCodec dataCodec) {
        delegates.forEach(m -> m.setDataCodec(dataCodec));
        return this;
    }
}
