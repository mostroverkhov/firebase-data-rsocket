package com.github.mostroverkhov.firebase_rsocket.internal.codec;

import com.github.mostroverkhov.firebase_rsocket_data.common.Conversions;
import com.google.gson.Gson;

import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class GsonDataCodec implements DataCodec {
    private final Gson gson;
    private Charset charSet;

    public GsonDataCodec(Gson gson, Charset charSet) {
        this.gson = gson;
        this.charSet = charSet;
    }

    @Override
    public byte[] encode(Object data) {
        return gson.toJson(data).getBytes(charSet);
    }

    @Override
    public <T> T decode(byte[] data, Class<T> clazz) {
        return gson.fromJson(Conversions.bytesToReader(data, charSet), clazz);
    }
}
