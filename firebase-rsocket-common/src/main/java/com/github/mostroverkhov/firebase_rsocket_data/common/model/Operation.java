package com.github.mostroverkhov.firebase_rsocket_data.common.model;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public abstract class Operation {
    private volatile String operation;

    public Operation(String operation) {
        this.operation = operation;
    }

    public String getOp() {
        return operation;
    }
}
