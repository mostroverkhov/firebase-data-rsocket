package com.github.mostroverkhov.firebase_rsocket.internal.codec;

import com.github.mostroverkhov.firebase_rsocket.servercommon.KeyValue;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface MetadataCodec {
    byte[] encode(KeyValue keyValue);

    KeyValue decode(byte[] metadata);
}
