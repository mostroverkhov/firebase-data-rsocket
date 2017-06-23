package com.github.mostroverkhov.firebase_rsocket.internal.handler;

import com.github.mostroverkhov.firebase_rsocket.servercommon.KeyValue;
import io.reactivex.Flowable;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface ServerRequestHandler<Req, Resp> {

    boolean canHandle(KeyValue metadata);

    Flowable<Resp> handle(KeyValue metadata, Req req);

    @SuppressWarnings("unchecked")
    default Flowable<Resp> handleOp(KeyValue metadata, Object request) {
        return handle(metadata, (Req) request);
    }
}