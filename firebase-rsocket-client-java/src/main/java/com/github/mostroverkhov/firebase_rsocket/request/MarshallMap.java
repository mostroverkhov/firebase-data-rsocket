package com.github.mostroverkhov.firebase_rsocket.request;

import io.reactivesocket.Payload;
import org.reactivestreams.Publisher;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface MarshallMap<Req, Resp> {

    Payload marshallRequest(Req request);

    Publisher<Resp> mapResponse(Payload response);
}
