package com.github.mostroverkhov.firebase_rsocket.server.tcp.entrypoint;

import java.util.Objects;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
class Configuration {
    private final Integer port;
    private final String credsFile;

    public Configuration(Integer port,
                         String credsFile) {
        Objects.requireNonNull(port, "port");
        Objects.requireNonNull(credsFile, "credsFile");
        this.port = port;
        this.credsFile = credsFile;
    }

    public Integer getPort() {
        return port;
    }

    public String getCredsFile() {
        return credsFile;
    }
}
