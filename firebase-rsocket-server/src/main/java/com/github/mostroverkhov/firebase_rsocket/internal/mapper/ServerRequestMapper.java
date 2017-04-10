package com.github.mostroverkhov.firebase_rsocket.internal.mapper;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;

import java.util.Optional;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public interface ServerRequestMapper<T extends Operation> {

    boolean accepts(byte[] metaData);

    Optional<T> map(byte[] metadata, byte[] data);

    byte[] marshall(Object response);
}
