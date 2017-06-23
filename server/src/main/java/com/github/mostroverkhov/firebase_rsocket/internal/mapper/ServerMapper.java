package com.github.mostroverkhov.firebase_rsocket.internal.mapper;

import com.github.mostroverkhov.firebase_rsocket.servercommon.KeyValue;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.HasCodec;

import java.util.Optional;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public interface ServerMapper<T> extends HasCodec {

    boolean accepts(KeyValue metaData);

    Optional<T> map(KeyValue metadata, byte[] data);

    byte[] marshall(Object response);
}
