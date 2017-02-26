package com.github.mostroverkhov.firebase_rsocket.handlers.requesthandlers.impl;

import com.github.mostroverkhov.firebase_rsocket.FirebaseRsocketMessageFormatException;
import com.github.mostroverkhov.firebase_rsocket.ServerSocketAcceptor;
import com.github.mostroverkhov.firebase_rsocket.handlers.requesthandlers.RequestHandler;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;
import io.reactivesocket.Payload;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class UnknownHandler implements RequestHandler {

    @Override
    public boolean canHandle(Operation op) {
        return true;
    }

    @Override
    public Publisher<Payload> handle(ServerSocketAcceptor.SocketContext context, Operation op) {
        return Flowable.error(unknownOperationError(op.getOp()));

    }

    private Callable<Throwable> unknownOperationError(String operation) {
        String msg = operation.isEmpty() ? " empty" : operation;
        return () -> new FirebaseRsocketMessageFormatException(
                "No handler for operation: " + msg);
    }

}
