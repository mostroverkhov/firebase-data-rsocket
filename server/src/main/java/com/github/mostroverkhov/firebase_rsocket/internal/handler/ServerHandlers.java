package com.github.mostroverkhov.firebase_rsocket.internal.handler;


import com.github.mostroverkhov.firebase_rsocket.servercommon.KeyValue;

import java.util.Queue;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class ServerHandlers {

    private final Queue<ServerRequestHandler<?, ?>> handlers;

    private ServerHandlers(Queue<ServerRequestHandler<?, ?>> handlers) {
        assertHandlers(handlers);
        this.handlers = handlers;
    }

    public static ServerHandlers newInstance(Queue<ServerRequestHandler<?, ?>> handlers) {
        return new ServerHandlers(handlers);
    }

    public ServerRequestHandler<?, ?> handlerFor(KeyValue metadata) {
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
