package com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.write;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.Serializer;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.GsonClientCodec;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteResponse;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class WritePushClientCodec<T> extends GsonClientCodec<WriteRequest<T>, WriteResponse> {

    public WritePushClientCodec(Serializer serializer) {
        super(WriteResponse.class, serializer);
    }
}
