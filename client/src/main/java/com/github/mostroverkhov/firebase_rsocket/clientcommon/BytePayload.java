package com.github.mostroverkhov.firebase_rsocket.clientcommon;

public class BytePayload {
    private final byte[] metaData;
    private final byte[] data;

    public BytePayload(byte[] metaData, byte[] data) {
        this.metaData = metaData;
        this.data = data;
    }

    public byte[] getMetaData() {
        return metaData;
    }

    public byte[] getData() {
        return data;
    }
}