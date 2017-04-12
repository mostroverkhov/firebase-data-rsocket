package com.github.mostroverkhov.firebase_rsocket.internal.mapper;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.CodecAware;
import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;

import java.util.Optional;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public interface ServerRequestMapper<T> extends CodecAware {

    boolean accepts(KeyValue metaData);

    Optional<T> map(KeyValue metadata, byte[] data);

    byte[] marshall(Object response);
}
