package com.github.mostroverkhov.firebase_rsocket.request;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;
import io.reactivesocket.Payload;
import org.reactivestreams.Publisher;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface ClientMapper<Req extends Operation, Resp> {

    Payload marshallRequest(Req request);

    Publisher<Resp> mapResponse(Payload response);
}
