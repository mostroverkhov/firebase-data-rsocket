package com.github.mostroverkhov.firebase_rsocket.internal.handler;

import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class ServerHandlers {

    private final List<ServerRequestHandler<?, ?>> handlers;

    public ServerHandlers(ServerRequestHandler<?, ?>... handlers) {
        assertHandlers(handlers);
        this.handlers = Arrays.asList(handlers);
    }

    private ServerHandlers(List<ServerRequestHandler<?, ?>> handlers) {
        assertHandlers(handlers);
        this.handlers = handlers;
    }

    public static ServerHandlers newInstance(List<ServerRequestHandler<?, ?>> handlers) {
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
