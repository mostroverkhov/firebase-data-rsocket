package com.github.mostroverkhov.firebase_rsocket;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class LogConfig {
    private final Logger logger;

    public LogConfig(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }

}
