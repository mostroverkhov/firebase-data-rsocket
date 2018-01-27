package com.github.mostroverkhov.firebase_rsocket.model.write;


import com.github.mostroverkhov.firebase_rsocket.model.Path;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public class WriteRequest<T> {
    private final Path path;
    private final T data;

    public WriteRequest(Path path,
                        T data) {
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
                ", path=" + path +
                ", data=" + data +
                '}';
    }
}
