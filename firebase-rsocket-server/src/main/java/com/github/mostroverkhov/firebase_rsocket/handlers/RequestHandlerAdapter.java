package com.github.mostroverkhov.firebase_rsocket.handlers;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;

import java.util.Optional;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public interface RequestHandlerAdapter<T extends Operation> {
    Optional<T> adapt(String request);
}
