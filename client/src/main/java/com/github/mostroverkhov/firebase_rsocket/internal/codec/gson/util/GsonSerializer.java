package com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.util;

import com.google.gson.Gson;

import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class GsonSerializer {
    private final String encoding;
    private final Gson gson;
    private final Charset charset;

    public GsonSerializer(Gson gson, String encoding) {
        this.encoding = encoding;
        this.charset = Charset.forName(encoding);
        this.gson = gson;
    }

    public String getEncoding() {
        return encoding;
    }

    public Charset getCharset() {
        return charset;
    }

    public Gson getGson() {
        return gson;
    }
}
