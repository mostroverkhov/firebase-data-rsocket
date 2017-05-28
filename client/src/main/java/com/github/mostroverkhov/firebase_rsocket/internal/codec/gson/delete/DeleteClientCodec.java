package com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.delete;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.Serializer;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.GsonClientCodec;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteResponse;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DeleteClientCodec extends GsonClientCodec<DeleteRequest, DeleteResponse> {

    public DeleteClientCodec(Serializer serializer) {
        super(DeleteResponse.class, serializer);
    }
}
