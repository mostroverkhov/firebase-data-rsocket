package com.github.mostroverkhov.firebase_rsocket;

/**
 * Created by Maksym Ostroverkhov on 01.03.17.
 */
public class Data {
    private String data;
    private String id;

    public Data(String data, String id) {
        this.data = data;
        this.id = id;
    }

    public Data() {
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
