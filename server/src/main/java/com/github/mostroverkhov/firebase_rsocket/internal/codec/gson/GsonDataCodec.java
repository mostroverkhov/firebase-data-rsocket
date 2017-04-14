package com.github.mostroverkhov.firebase_rsocket.internal.codec.gson;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.DataCodec;
import com.github.mostroverkhov.firebase_rsocket_data.common.Conversions;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

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
    public Optional<String> decode(byte[] data) {
        String s;
        try {
            s = IOUtils.toString(data, charSet.name());
        } catch (IOException e) {
            return Optional.empty();
        }
        return Optional.of(s);

    }

    @Override
    public <T> T decode(byte[] data, Class<T> clazz) {
        return gson.fromJson(Conversions.bytesToReader(data, charSet), clazz);
    }
}
