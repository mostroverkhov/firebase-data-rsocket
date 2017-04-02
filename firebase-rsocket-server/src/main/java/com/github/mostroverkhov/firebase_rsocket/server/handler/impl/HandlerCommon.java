package com.github.mostroverkhov.firebase_rsocket.server.handler.impl;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Path;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import io.reactivesocket.Payload;
import io.reactivesocket.util.PayloadImpl;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class HandlerCommon {
    public static DatabaseReference reference(Path path) {
        DatabaseReference dataRef = FirebaseDatabase.getInstance()
                .getReference();
        for (String s : Arrays.asList(path.getChildPaths())) {
            dataRef = dataRef.child(s);
        }
        return dataRef;
    }

    public static Payload payload(Gson gson, Object dw) {
        String data = gson.toJson(dw);
        return new PayloadImpl(data);
    }

}
