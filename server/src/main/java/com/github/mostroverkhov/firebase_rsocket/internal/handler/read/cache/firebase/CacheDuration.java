package com.github.mostroverkhov.firebase_rsocket.internal.handler.read.cache.firebase;


import com.github.mostroverkhov.firebase_rsocket.servercommon.model.read.ReadRequest;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface CacheDuration {

    long getDuration();

    void readRequest(ReadRequest request);
}
