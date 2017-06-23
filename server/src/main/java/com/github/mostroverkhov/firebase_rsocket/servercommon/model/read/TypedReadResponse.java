package com.github.mostroverkhov.firebase_rsocket.servercommon.model.read;

import java.util.List;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
public final class TypedReadResponse<T> {
    private final ReadRequest readRequest;
    private final List<T> data;

    public TypedReadResponse(ReadRequest readRequest, List<T> data) {
        this.readRequest = readRequest;
        this.data = data;
    }

    public ReadRequest getReadRequest() {
        return readRequest;
    }

    public List<T> getData() {
        return data;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReadResponse{");
        sb.append("readRequest=").append(readRequest);
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}
