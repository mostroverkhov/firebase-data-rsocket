package com.github.mostroverkhov.firebase_rsocket.server.tcp.entrypoint;

import java.util.Optional;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
class Configuration {
    private final Optional<String> port;
    private final Optional<String> credsFile;

    public Configuration(Optional<String> port,
                         Optional<String> credsFile) {
        this.port = port;
        this.credsFile = credsFile;
    }

    public Optional<String> getPort() {
        return port;
    }

    public Optional<String> getCredsFile() {
        return credsFile;
    }
}
