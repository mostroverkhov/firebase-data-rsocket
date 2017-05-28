package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.ClientCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.GsonClientCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.util.GsonSerializer;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class GsonCodecFactory implements CodecFactory {
    private final GsonSerializer gsonSerializer;

    public GsonCodecFactory(GsonSerializer gsonSerializer) {
        this.gsonSerializer = gsonSerializer;
    }

    @Override
    public <Req, Resp> ClientCodec<Req, Resp> getCodec(Class<Req> req, Class<Resp> resp) {
        return new GsonClientCodec<>(resp, gsonSerializer);
    }
}
