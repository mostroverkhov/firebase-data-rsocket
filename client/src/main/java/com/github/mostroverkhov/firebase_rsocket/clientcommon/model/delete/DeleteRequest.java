package com.github.mostroverkhov.firebase_rsocket.clientcommon.model.delete;

import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.Path;

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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeleteRequest{");
        sb.append("path=").append(path);
        sb.append('}');
        return sb.toString();
    }
}
