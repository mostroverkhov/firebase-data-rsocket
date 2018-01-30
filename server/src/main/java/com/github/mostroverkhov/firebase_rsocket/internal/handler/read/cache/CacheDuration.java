package com.github.mostroverkhov.firebase_rsocket.internal.handler.read.cache;


import com.github.mostroverkhov.firebase_rsocket.model.read.ReadRequest;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface CacheDuration {

    long getDurationSeconds(ReadRequest request);
}
