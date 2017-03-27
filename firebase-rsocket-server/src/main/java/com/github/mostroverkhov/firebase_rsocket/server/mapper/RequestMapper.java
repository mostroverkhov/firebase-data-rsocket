package com.github.mostroverkhov.firebase_rsocket.server.mapper;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;

import java.util.Optional;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public interface RequestMapper<T extends Operation> {
    Optional<T> map(byte[] request);
}
