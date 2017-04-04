package com.github.mostroverkhov.firebase_rsocket.internal.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public class DelegatingRequestMapper implements ServerRequestMapper {
    private final List<ServerRequestMapper> delegateAdapters = new ArrayList<>();

    public DelegatingRequestMapper(ServerRequestMapper<?>... adapters) {
        assertAdapters(adapters);
        delegateAdapters.addAll(Arrays.asList(adapters));
    }

    @Override
    public Optional<?> map(byte[] request) {
        return delegateAdapters.stream()
                .map(adapter -> adapter.map(request))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    @Override
    public byte[] marshall(Object response) {
        return delegateAdapters.get(0).marshall(response);
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
