package com.github.mostroverkhov.firebase_rsocket.internal.codec;

import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;
import com.github.mostroverkhov.firebase_rsocket_data.common.BytePayload;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface ClientCodec {

    BytePayload encode(KeyValue metadata, Object request);

    <Resp> Resp decode(byte[] data, Class<Resp> type);
}
