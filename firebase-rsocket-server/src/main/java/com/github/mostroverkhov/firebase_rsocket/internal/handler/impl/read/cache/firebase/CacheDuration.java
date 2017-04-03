package com.github.mostroverkhov.firebase_rsocket.internal.handler.impl.read.cache.firebase;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface CacheDuration {

    long getDuration();

    void readRequest(ReadRequest request);
}
