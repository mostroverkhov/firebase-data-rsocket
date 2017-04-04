package com.github.mostroverkhov.firebase_rsocket;

import com.google.gson.Gson;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
class ClientContext {
    private final Gson gson;

    public ClientContext(Gson gson) {
        this.gson = gson;
    }

    public Gson gson() {
        return gson;
    }
}
