package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Path;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteRequest;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public class WriteRequestBuilder<T> {
    private final String[] childPaths;
    private T data;

    public WriteRequestBuilder(String[] childPaths) {
        this.childPaths = childPaths;
    }

    public WriteRequestBuilder<T> data(T data) {
        this.data = data;
        return this;
    }

    public WriteRequest<T> build() {
        assertData(data, "Data should not be null");
        assertPaths(childPaths);

        return new WriteRequest<>(Op.WRITE_PUSH.code(),
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
