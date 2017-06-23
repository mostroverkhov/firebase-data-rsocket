package com.github.mostroverkhov.firebase_rsocket.clientcommon;

import io.reactivesocket.Payload;
import io.reactivesocket.util.PayloadImpl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public final class Conversions {

    public static byte[] stringToBytes(String str, Charset charset) {
        return str.getBytes(charset);
    }

    public static BufferedReader bytesToReader(byte[] bytes, Charset charSet) {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), charSet));
    }

    public static byte[] dataToBytes(Payload payload) {
        return byteBufferToBytes(payload.getData());
    }

    public static byte[] metadataToBytes(Payload payload) {
        return byteBufferToBytes(payload.getMetadata());
    }

    private static byte[] byteBufferToBytes(ByteBuffer bb) {
        byte[] b = new byte[bb.remaining()];
        bb.get(b);
        return b;
    }

    public static Payload bytesToPayload(byte[] bytes) {
        return new PayloadImpl(bytes);
    }

    public static Payload bytesToPayload(byte[] data, byte[] metadata) {
        return new PayloadImpl(data, metadata);
    }
}
