package com.github.mostroverkhov.firebase_rsocket.transport.aeron;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public final class AeronDriver {
    private AeronDriver() {
    }

    public static void load() {
        MediaDriverHolder.getInstance();
    }
}
