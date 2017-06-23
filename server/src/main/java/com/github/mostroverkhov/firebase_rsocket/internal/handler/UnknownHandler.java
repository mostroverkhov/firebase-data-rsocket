package com.github.mostroverkhov.firebase_rsocket.internal.handler;

import com.github.mostroverkhov.firebase_rsocket.FirebaseRsocketException;
import com.github.mostroverkhov.firebase_rsocket.servercommon.KeyValue;
import io.reactivex.Flowable;

import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class UnknownHandler implements ServerRequestHandler {

    @Override
    public boolean canHandle(KeyValue metadata) {
        return true;
    }

    @Override
    public Flowable handle(KeyValue metadata, Object req) {
        Optional<Object> op = Optional.ofNullable(metadata.get("operation"));
        return Flowable.error(unknownOperationError(op));
    }

    private Callable<Throwable> unknownOperationError(Optional<Object> operation) {
        String msg = operation.isPresent() ? operation.toString() : " empty";
        return () -> new FirebaseRsocketException(
                "No handler for operation: " + msg);
    }

}
