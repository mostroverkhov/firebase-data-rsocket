package com.github.mostroverkhov.firebase_rsocket_data.common.model;

public enum Op {

    DATA_WINDOW("data_window"),
    WRITE_PUSH("write_push");

    private final String code;

    Op(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}