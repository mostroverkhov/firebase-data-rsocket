package com.github.mostroverkhov.firebase_rsocket.handlers.requesthandlers;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class HandlerManager {

    private final List<RequestHandler> handlers;

    public HandlerManager(RequestHandler... handlers) {
        assertHandlers(handlers);
        this.handlers = Arrays.asList(handlers);
    }

    public HandlerManager(List<RequestHandler> handlers) {
        this.handlers = handlers;
    }

    public RequestHandler handler(Operation op) {
        return handlers.stream()
                .filter(h -> h.canHandle(op))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("Handlers chain is not exhaustive"));
    }

    private void assertHandlers(RequestHandler[] handlers) {
        if (handlers == null) {
            throw new IllegalArgumentException("handlers should not be null");
        }
    }

}
