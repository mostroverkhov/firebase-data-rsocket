package com.github.mostroverkhov.firebase_rsocket.handlers;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteRequest;
import com.google.gson.Gson;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public class DefaultHandlerAdapter extends DelegatingRequestHandlerAdapter {

    public DefaultHandlerAdapter(Gson gson) {
        super(new OperationBasedHandlerAdapter<>(gson, ReadRequest.class, Op.DATA_WINDOW.code()),
                new OperationBasedHandlerAdapter<>(gson, WriteRequest.class, Op.WRITE_PUSH.code()));
    }
}
