package com.github.mostroverkhov.firebase_rsocket.internal.handler.read.cache;

import com.github.mostroverkhov.firebase_rsocket.model.read.ReadRequest;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class CacheDurationConstant implements CacheDuration {
    private final long cacheDurationSeconds;

    public CacheDurationConstant(int units, TimeUnit timeUnit) {
        cacheDurationSeconds = timeUnit.toSeconds(units);
    }

    @Override
    public long getDurationSeconds(ReadRequest request) {
        return cacheDurationSeconds;
    }
}
