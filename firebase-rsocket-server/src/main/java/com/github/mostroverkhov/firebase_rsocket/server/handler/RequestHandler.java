package com.github.mostroverkhov.firebase_rsocket.server.handler;

import com.github.mostroverkhov.firebase_rsocket.ServerSocketAcceptor;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;
import io.reactivesocket.Payload;
import org.reactivestreams.Publisher;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface RequestHandler {
    Publisher<Payload> handle(final ServerSocketAcceptor.SocketContext context,
                              final Operation op);

    boolean canHandle(Operation op);
}