package com.github.mostroverkhov.firebase_rsocket.server.cache.firebase;

import com.google.firebase.database.DatabaseReference;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class NoNativeCache implements NativeCache {

    @Override
    public void cache(DatabaseReference ref, long duration, TimeUnit timeUnit) {

    }
}
