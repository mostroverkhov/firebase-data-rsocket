package com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.read;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.Serializer;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.GsonClientCodec;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.NonTypedReadResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DataWindowClientCodec extends GsonClientCodec<ReadRequest, NonTypedReadResponse> {

    public DataWindowClientCodec(Serializer serializer) {
        super(NonTypedReadResponse.class, serializer);
    }
}
