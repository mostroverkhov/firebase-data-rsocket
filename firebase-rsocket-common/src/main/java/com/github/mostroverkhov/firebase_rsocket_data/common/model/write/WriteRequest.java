package com.github.mostroverkhov.firebase_rsocket_data.common.model.write;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Path;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public class WriteRequest<T> implements Operation {
    private final String operation;
    private final Path path;
    private final T data;

    public WriteRequest(String operation,
                        Path path,
                        T data) {
        this.path = path;
        this.data = data;
        this.operation = operation;
    }

    public Path getPath() {
        return path;
    }

    public T getData() {
        return data;
    }

    @Override
    public String getOp() {
        return operation;
    }

    @Override
    public String toString() {
        return "WriteQuery{" +
                "operation='" + operation + '\'' +
                ", path=" + path +
                ", data=" + data +
                '}';
    }
}
