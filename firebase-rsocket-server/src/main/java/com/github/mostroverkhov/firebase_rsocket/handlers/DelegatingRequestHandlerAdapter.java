package com.github.mostroverkhov.firebase_rsocket.handlers;

import javax.swing.text.html.Option;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public class DelegatingRequestHandlerAdapter implements RequestHandlerAdapter {
    private final Set<RequestHandlerAdapter> delegateAdapters = new HashSet<>();

    public DelegatingRequestHandlerAdapter(RequestHandlerAdapter<?>... adapters) {
        assertAdapters(adapters);
        delegateAdapters.addAll(Arrays.asList(adapters));
    }

    @Override
    public Optional<?> adapt(String request) {
        return delegateAdapters.stream()
                .map(adapter -> adapter.adapt(request))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private static void assertAdapters(RequestHandlerAdapter<?>[] adapters) {
        if (adapters == null) {
            throw new IllegalArgumentException("Adapters should not be null");
        }
    }
}
