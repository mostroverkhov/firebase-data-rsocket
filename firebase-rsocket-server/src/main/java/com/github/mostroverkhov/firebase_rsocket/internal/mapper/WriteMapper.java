package com.github.mostroverkhov.firebase_rsocket.internal.mapper;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteRequest;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class WriteMapper extends MetadataMapper<WriteRequest> {
    public WriteMapper(String key, String... values) {
        super(WriteRequest.class, key, values);
    }
}
