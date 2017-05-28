package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.ClientCodec;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface CodecFactory {

    <Req, Resp> ClientCodec<Req, Resp> getCodec(Class<Req> req, Class<Resp> resp);
}
