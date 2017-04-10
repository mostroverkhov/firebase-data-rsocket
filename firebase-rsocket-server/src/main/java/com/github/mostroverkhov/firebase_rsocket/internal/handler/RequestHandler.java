package com.github.mostroverkhov.firebase_rsocket.internal.handler;

import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;
import io.reactivex.Flowable;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface RequestHandler<Req, Resp> {

    boolean canHandle(KeyValue metadata);

    Flowable<Resp> handle(Req req);

    @SuppressWarnings("unchecked")
    default Flowable<Resp> handleOp(Object request) {
        return handle((Req) request);
    }
}