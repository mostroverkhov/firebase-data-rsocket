package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Path;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteRequest;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DeleteRequestBuilder {
    private final String[] childrenPath;

    DeleteRequestBuilder(String[] childrenPath) {
        this.childrenPath = childrenPath;
    }

    public DeleteRequest build() {
        return new DeleteRequest(new Path(childrenPath));
    }
}
