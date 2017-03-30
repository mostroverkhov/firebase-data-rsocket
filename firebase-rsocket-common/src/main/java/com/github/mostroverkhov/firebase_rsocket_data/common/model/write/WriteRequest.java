package com.github.mostroverkhov.firebase_rsocket_data.common.model.write;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Path;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public class WriteRequest<T> extends Operation {
    private final Path path;
    private final T data;

    public WriteRequest(String operation,
                        Path path,
                        T data) {
        super(operation);
        this.path = path;
        this.data = data;
    }

    public Path getPath() {
        return path;
    }

    public T getData() {
        return data;
    }

    @Override
    public String toString() {
        return "WriteQuery{" +
                "operation='" + getOp() + '\'' +
                ", path=" + path +
                ", data=" + data +
                '}';
    }
}
