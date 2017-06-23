package com.github.mostroverkhov.firebase_rsocket.internal.handler;

import com.github.mostroverkhov.firebase_rsocket.internal.handler.ServerRequestHandler;
import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Path;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public abstract class MetadataRequestHandler<Req, Resp> implements ServerRequestHandler<Req, Resp> {
    private final String key;
    private final String value;

    public MetadataRequestHandler(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean canHandle(KeyValue metadata) {
        return value.equals(metadata.get(key));
    }

    protected DatabaseReference reference(Path path) {
        DatabaseReference dataRef = FirebaseDatabase.getInstance()
                .getReference();
        for (String s : Arrays.asList(path.getChildPaths())) {
            dataRef = dataRef.child(s);
        }
        return dataRef;
    }
}
