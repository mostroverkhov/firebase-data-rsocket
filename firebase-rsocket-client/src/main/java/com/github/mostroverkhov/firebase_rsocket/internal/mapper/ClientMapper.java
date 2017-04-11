package com.github.mostroverkhov.firebase_rsocket.internal.mapper;

import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;
import com.github.mostroverkhov.firebase_rsocket_data.common.BytePayload;
import org.reactivestreams.Publisher;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface ClientMapper<Req, Resp> {

    BytePayload marshall(KeyValue metadata, Req request);

    Publisher<Resp> map(byte[] response);
}
