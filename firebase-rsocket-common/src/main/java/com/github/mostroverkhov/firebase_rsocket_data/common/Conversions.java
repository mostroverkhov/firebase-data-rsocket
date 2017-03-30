package com.github.mostroverkhov.firebase_rsocket_data.common;

import io.reactivesocket.Payload;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public final class Conversions {

    public static String bytesToString(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("String encoding error", e);
        }
    }

    public static byte[] bytes(Payload payload) {
        ByteBuffer bb = payload.getData();
        byte[] b = new byte[bb.remaining()];
        bb.get(b);
        return b;
    }

    public static Reader payloadReader(Payload payload) {
        byte[] bytes = Conversions.bytes(payload);
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
    }
}
