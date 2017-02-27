package com.github.mostroverkhov.firebase_rsocket_server.model;

/**
 * Created by Maksym Ostroverkhov on 27.02.17.
 */
public class Path {
    private final String dataPath;

    public Path(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getDataPath() {
        return dataPath;
    }
}
