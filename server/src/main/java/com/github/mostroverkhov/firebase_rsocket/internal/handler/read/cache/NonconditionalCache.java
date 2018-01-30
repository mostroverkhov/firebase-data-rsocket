package com.github.mostroverkhov.firebase_rsocket.internal.handler.read.cache;

import com.google.firebase.database.DatabaseReference;

import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class NonconditionalCache implements NativeCache {
    private final ScheduledExecutorService service;

    public NonconditionalCache(ScheduledExecutorService service) {
        assertNotNull(service);
        this.service = service;

    }

    @Override
    public void cache(DatabaseReference ref, long duration, TimeUnit timeUnit) {
        assertNotNull(ref, timeUnit);
        ref.keepSynced(true);
        service.schedule(() -> ref.keepSynced(false), duration, timeUnit);
    }

    private static void assertNotNull(Object... args) {
        for (Object arg : args) {
            if (arg == null) {
                throw new IllegalArgumentException("Args should not be null: " + Arrays.toString(args));
            }
        }
    }
}
