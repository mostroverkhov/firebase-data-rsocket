package com.github.mostroverkhov.firebase_rsocket.internal.mapper;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;
import org.reactivestreams.Publisher;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface ClientMapper<Req extends Operation, Resp> {

    byte[] marshall(Req request);

    Publisher<Resp> map(byte[] response);
}
