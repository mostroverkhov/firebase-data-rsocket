package com.github.mostroverkhov.firebase_rsocket_data.common.model.delete;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Path;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DeleteRequest implements Operation {

    private final String operation;
    private final Path path;

    public DeleteRequest(String operation, Path path) {
        this.operation = operation;
        this.path = path;
    }

    @Override
    public String getOp() {
        return operation;
    }

    public Path getPath() {
        return path;
    }
}
