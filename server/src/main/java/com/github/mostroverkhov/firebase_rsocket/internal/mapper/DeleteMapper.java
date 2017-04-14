package com.github.mostroverkhov.firebase_rsocket.internal.mapper;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteRequest;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DeleteMapper extends MetadataMapper<DeleteRequest> {
    public DeleteMapper(String key, String... values) {
        super(DeleteRequest.class, key, values);
    }
}
