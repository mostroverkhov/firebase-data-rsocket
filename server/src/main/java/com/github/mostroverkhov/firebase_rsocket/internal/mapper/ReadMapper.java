package com.github.mostroverkhov.firebase_rsocket.internal.mapper;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class ReadMapper extends MetadataMapper<ReadRequest> {
    public ReadMapper(String key, String... values) {
        super(ReadRequest.class, key, values);
    }
}
