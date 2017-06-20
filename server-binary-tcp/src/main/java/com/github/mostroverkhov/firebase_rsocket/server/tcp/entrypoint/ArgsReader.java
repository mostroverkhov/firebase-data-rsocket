package com.github.mostroverkhov.firebase_rsocket.server.tcp.entrypoint;

import java.util.Optional;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
class ArgsReader {

    public Configuration read(String[] args) {
        Optional<String> serverPort = serverPort();
        Optional<String> credsFile = credsFile();
        return new Configuration(serverPort, credsFile);
    }

    static Optional<String> credsFile() {
        return property("server.creds");
    }

    static Optional<String> serverPort() {
        return property("server.port");
    }

    static Optional<String> property(String name) {
        String prop = System.getProperty(name);
        return Optional.ofNullable(prop);
    }

}
