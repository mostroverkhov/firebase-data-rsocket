package com.github.mostroverkhov.firebase_rsocket.api;

import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.Path;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.write.WriteRequest;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public class WriteBuilder<T> {
    private final String[] childPaths;
    private T data;

    WriteBuilder(String[] childPaths) {
        this.childPaths = childPaths;
    }

    public WriteBuilder<T> data(T data) {
        this.data = data;
        return this;
    }

    public WriteRequest<T> build() {
        assertData(data, "Data should not be null");
        assertPaths(childPaths);

        return new WriteRequest<>(
                new Path(childPaths),
                data);
    }

    private static void assertData(Object data, String s) {
        if (data == null) {
            throw new IllegalArgumentException(s);
        }
    }

    private static void assertPaths(String[] childPaths) {
        assertData(childPaths, "Child paths should not be null");
    }
}
