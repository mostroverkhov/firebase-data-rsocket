package com.github.mostroverkhov.firebase_rsocket.internal.mapper;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public class DelegatingRequestMapper implements ServerRequestMapper {
    private final List<ServerRequestMapper<?>> delegates = new ArrayList<>();

    public DelegatingRequestMapper(ServerRequestMapper<?>... adapters) {
        assertAdapters(adapters);
        delegates.addAll(Arrays.asList(adapters));
    }

    @Override
    public boolean accepts(byte[] metaData) {
        return true;
    }

    @Override
    public Optional<? extends Operation> map(byte[] metadata, byte[] data) {

        Optional<ServerRequestMapper<?>> maybeMapper = delegates
                .stream()
                .filter(mapper -> mapper.accepts(metadata))
                .findFirst();

        Optional<? extends Operation> maybeOp =
                maybeMapper.flatMap(mapper -> mapper.map(metadata, data));

        return maybeOp;

    }

    @Override
    public byte[] marshall(Object response) {
        return delegates.get(0).marshall(response);
    }

    private static void assertAdapters(ServerRequestMapper<?>[] adapters) {
        if (adapters == null) {
            throw new IllegalArgumentException("Adapters should not be null");
        }
        if (adapters.length == 0) {
            throw new IllegalArgumentException("Adapters should not be empty");
        }
    }
}
