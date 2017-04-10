package com.github.mostroverkhov.firebase_rsocket_data.common.model;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public abstract class Operation {
    private volatile String operation = "";

    public Operation(String operation) {
        assertNotEmpty(operation);
        this.operation = operation;
    }

    public Operation() {
    }

    public String getOp() {
        return operation;
    }

    private void assertNotEmpty(String operation) {
        if (operation == null || operation.isEmpty()) {
            throw new IllegalArgumentException("Operation should not be empty");
        }
    }
}
