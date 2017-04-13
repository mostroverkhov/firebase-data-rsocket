package com.github.mostroverkhov.firebase_rsocket.internal.codec;

import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;
import com.github.mostroverkhov.firebase_rsocket_data.common.BytePayload;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface ClientCodec<Req, Resp> {

    BytePayload marshall(KeyValue metadata, Req request);

    Resp map(byte[] response);

    void setSerializer(Serializer serializer);
}
