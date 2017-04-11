package com.github.mostroverkhov.firebase_rsocket.internal.codec;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface DataCodec {
    byte[] encode(Object data);

    <T> T decode(byte[] data, Class<T> clazz);
}
