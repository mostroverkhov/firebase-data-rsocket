package com.github.mostroverkhov.firebase_rsocket.api;

import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.Path;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.delete.DeleteRequest;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DeleteBuilder {
    private final String[] childrenPath;

    DeleteBuilder(String[] childrenPath) {
        this.childrenPath = childrenPath;
    }

    public DeleteRequest build() {
        return new DeleteRequest(new Path(childrenPath));
    }
}
