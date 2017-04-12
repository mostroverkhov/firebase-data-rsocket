package com.github.mostroverkhov.firebase_rsocket.internal.codec;

import java.util.Optional;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface DataCodec {

    byte[] encode(Object data);

    Optional<String> decode(byte[] data);

    <T> T decode(byte[] data, Class<T> clazz);
}
