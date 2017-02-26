package com.github.mostroverkhov.firebase_rsocket_data.common.model.delete;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DeleteResponse {
    private final String[] pathChildren;

    public DeleteResponse(String[] pathChildren) {
        assertArgs(pathChildren);
        this.pathChildren = pathChildren;
    }

    public String[] getPathChildren() {
        return pathChildren;
    }

    private static void assertArgs(String[] pathChildren) {
        if (pathChildren == null) {
            throw new IllegalArgumentException("Path should not be null");
        }
    }
}
