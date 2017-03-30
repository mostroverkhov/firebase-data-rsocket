package com.github.mostroverkhov.firebase_rsocket.server.handler;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;
import io.reactivex.Flowable;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface RequestHandler<Req extends Operation, Resp> {

    boolean canHandle(Operation op);

    Flowable<Resp> handle(Req req);

    @SuppressWarnings("unchecked")
    default Flowable<Resp> handleOp(Operation operation) {
        return handle((Req) operation);
    }
}