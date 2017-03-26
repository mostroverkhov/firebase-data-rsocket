package com.github.mostroverkhov.firebase_rsocket_data.common.transport.aeron;

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
