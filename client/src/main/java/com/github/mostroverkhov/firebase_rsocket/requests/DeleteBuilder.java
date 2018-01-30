package com.github.mostroverkhov.firebase_rsocket.requests;


import com.github.mostroverkhov.firebase_rsocket.model.Path;
import com.github.mostroverkhov.firebase_rsocket.model.delete.DeleteRequest;

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
