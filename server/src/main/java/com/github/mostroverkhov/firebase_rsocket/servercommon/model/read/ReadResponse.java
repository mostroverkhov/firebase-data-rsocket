package com.github.mostroverkhov.firebase_rsocket.servercommon.model.read;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class ReadResponse {

    private final ReadRequest readRequest;
    private final String data;

    public ReadResponse(ReadRequest readRequest, String data) {
        this.readRequest = readRequest;
        this.data = data;
    }

    public ReadRequest getReadRequest() {
        return readRequest;
    }

    public String getData() {
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
