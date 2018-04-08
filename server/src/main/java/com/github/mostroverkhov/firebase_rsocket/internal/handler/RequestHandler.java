package com.github.mostroverkhov.firebase_rsocket.internal.handler;

import com.github.mostroverkhov.firebase_rsocket.model.Path;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import rx.Observable;

public abstract class RequestHandler {

    protected DatabaseReference reference(Path path) {
        DatabaseReference dataRef = FirebaseDatabase.getInstance()
                .getReference();
        for (String s : path.getChildPaths()) {
            dataRef = dataRef.child(s);
        }
        return dataRef;
    }

    protected <T> Flux<T> asFlux(Observable<T> observable) {
        return RxJava2Adapter.flowableToFlux(RxJavaInterop.toV2Flowable(observable));
    }
}
