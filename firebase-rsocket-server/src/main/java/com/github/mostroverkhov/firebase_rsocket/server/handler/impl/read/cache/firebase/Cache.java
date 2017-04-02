package com.github.mostroverkhov.firebase_rsocket.server.handler.impl.read.cache.firebase;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class Cache {
    private final NativeCache nativeCache;
    private final CacheDuration cacheDuration;

    public Cache(NativeCache nativeCache,
                 CacheDuration cacheDuration) {
        this.nativeCache = nativeCache;
        this.cacheDuration = cacheDuration;
    }

    public NativeCache nativeCache() {
        return nativeCache;
    }

    public CacheDuration cacheDuration() {
        return cacheDuration;
    }
}
