package com.github.mostroverkhov.firebase_rsocket.server.mapper;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteRequest;
import com.google.gson.Gson;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public class DefaultRequestMapper extends DelegatingRequestMapper {

    public DefaultRequestMapper(Gson gson) {
        super(new OperationBasedRequestMapper<>(gson, ReadRequest.class, Op.DATA_WINDOW.code()),
                new OperationBasedRequestMapper<>(gson, WriteRequest.class, Op.WRITE_PUSH.code()),
                new OperationBasedRequestMapper<>(gson, DeleteRequest.class, Op.DELETE.code()));
    }
}
