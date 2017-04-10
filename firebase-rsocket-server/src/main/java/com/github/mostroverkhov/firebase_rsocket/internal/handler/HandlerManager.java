package com.github.mostroverkhov.firebase_rsocket.internal.handler;

import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class HandlerManager {

    private final List<RequestHandler<?, ?>> handlers;

    public HandlerManager(RequestHandler<?, ?>... handlers) {
        assertHandlers(handlers);
        this.handlers = Arrays.asList(handlers);
    }

    public HandlerManager(List<RequestHandler<?, ?>> handlers) {
        assertHandlers(handlers);
        this.handlers = handlers;
    }

    public RequestHandler<?, ?> handlerFor(KeyValue metadata) {
        return handlers.stream()
                .filter(h -> h.canHandle(metadata))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("Handlers chain is not exhaustive"));
    }

    private void assertHandlers(Object handlers) {
        if (handlers == null) {
            throw new IllegalArgumentException("handlers should not be null");
        }
    }

}
