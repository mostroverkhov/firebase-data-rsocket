package com.github.mostroverkhov.firebase_rsocket.servercommon.model;

public enum Op {

    DATA_WINDOW("data_window"),
    DATA_WINDOW_NOTIF("data_window_notif"),
    WRITE_PUSH("write_push"),
    DELETE("delete");

    private final String value;

    Op(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static String key() {
        return "operation";
    }
}