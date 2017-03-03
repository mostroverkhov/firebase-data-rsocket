package com.github.mostroverkhov.firebase_rsocket_data.common.model.read;

import java.util.List;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
public final class ReadResponse<T> {
    private final ReadRequest readRequest;
    private final List<T> data;

    public ReadResponse(ReadRequest readRequest, List<T> data) {
        this.readRequest = readRequest;
        this.data = data;
    }

    public ReadRequest getReadRequest() {
        return readRequest;
    }

    public List<T> getData() {
        return data;
    }
}
