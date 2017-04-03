package com.github.mostroverkhov.firebase_rsocket.internal.handler.impl;

import com.github.mostroverkhov.firebase_rsocket.internal.handler.RequestHandler;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public abstract class BaseRequestHandler<Req extends Operation, Resp> implements RequestHandler<Req, Resp> {
    private final Op op;

    public BaseRequestHandler(Op op) {
        this.op = op;
    }

    @Override
    public boolean canHandle(Operation op) {
        return this.op.code().equals(op.getOp());
    }

    public Op getOp() {
        return op;
    }
}
