package com.github.mostroverkhov.firebase_rsocket.internal.handler.impl;

import com.github.mostroverkhov.firebase_rsocket.internal.handler.RequestHandler;
import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Path;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

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
    public boolean canHandle(KeyValue metadata) {
        return this.op.code().equals(metadata.get("operation"));
    }

    protected DatabaseReference reference(Path path) {
        DatabaseReference dataRef = FirebaseDatabase.getInstance()
                .getReference();
        for (String s : Arrays.asList(path.getChildPaths())) {
            dataRef = dataRef.child(s);
        }
        return dataRef;
    }
}
