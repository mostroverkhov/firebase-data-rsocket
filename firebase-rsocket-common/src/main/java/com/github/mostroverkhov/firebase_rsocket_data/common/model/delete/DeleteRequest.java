package com.github.mostroverkhov.firebase_rsocket_data.common.model.delete;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Path;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DeleteRequest {

    private final Path path;

    public DeleteRequest(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }
}
