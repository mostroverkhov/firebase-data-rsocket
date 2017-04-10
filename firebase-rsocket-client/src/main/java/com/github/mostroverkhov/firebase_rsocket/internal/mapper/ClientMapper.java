package com.github.mostroverkhov.firebase_rsocket.internal.mapper;

import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;
import com.github.mostroverkhov.firebase_rsocket_data.common.BytePayload;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;
import org.reactivestreams.Publisher;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface ClientMapper<Req extends Operation, Resp> {

    BytePayload marshall(Req request, KeyValue metadata);

    Publisher<Resp> map(byte[] response);
}
