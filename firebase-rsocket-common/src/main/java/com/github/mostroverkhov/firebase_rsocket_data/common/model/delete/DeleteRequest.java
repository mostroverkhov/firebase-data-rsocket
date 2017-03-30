package com.github.mostroverkhov.firebase_rsocket_data.common.model.delete;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Path;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DeleteRequest extends Operation {

    private final Path path;

    public DeleteRequest(String operation, Path path) {
        super(operation);
        this.path = path;
    }

    public Path getPath() {
        return path;
    }
}
