package com.github.mostroverkhov.firebase_rsocket_data.common.model;

/**
 * Created by Maksym Ostroverkhov on 27.02.17.
 */
public class Path {
    private final String[] childPaths;

    public Path(String... childPaths) {
        this.childPaths = childPaths;
    }

    public String[] getChildPaths() {
        return childPaths;
    }
}
