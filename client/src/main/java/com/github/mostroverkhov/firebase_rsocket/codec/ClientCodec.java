package com.github.mostroverkhov.firebase_rsocket.codec;


import com.github.mostroverkhov.firebase_rsocket.clientcommon.BytePayload;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.KeyValue;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface ClientCodec {

    BytePayload encode(KeyValue metadata, Object request);

    <Resp> Resp decode(byte[] data, Class<Resp> type);
}
