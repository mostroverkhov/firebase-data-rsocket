package com.github.mostroverkhov.firebase_rsocket.server.mapper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public class DelegatingRequestMapper implements RequestMapper {
    private final Set<RequestMapper> delegateAdapters = new HashSet<>();

    public DelegatingRequestMapper(RequestMapper<?>... adapters) {
        assertAdapters(adapters);
        delegateAdapters.addAll(Arrays.asList(adapters));
    }

    @Override
    public Optional<?> map(String request) {
        return delegateAdapters.stream()
                .map(adapter -> adapter.map(request))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private static void assertAdapters(RequestMapper<?>[] adapters) {
        if (adapters == null) {
            throw new IllegalArgumentException("Adapters should not be null");
        }
    }
}
