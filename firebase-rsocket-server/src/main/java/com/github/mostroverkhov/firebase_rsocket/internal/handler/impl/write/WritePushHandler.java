package com.github.mostroverkhov.firebase_rsocket.internal.handler.impl.write;

import com.github.mostroverkhov.firebase_data_rxjava.rx.FirebaseDatabaseManager;
import com.github.mostroverkhov.firebase_data_rxjava.rx.model.WriteResult;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.impl.BaseRequestHandler;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Path;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteResponse;
import com.google.firebase.database.DatabaseReference;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.Flowable;
import rx.Observable;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class WritePushHandler extends BaseRequestHandler<WriteRequest<?>, WriteResponse> {

    public WritePushHandler() {
        super(Op.WRITE_PUSH);
    }

    @Override
    public Flowable<WriteResponse> handle(WriteRequest<?> writeRequest) {

        Path path = writeRequest.getPath();
        DatabaseReference dbRef = reference(path);
        DatabaseReference newKeyRef = dbRef.push();

        Object data = writeRequest.getData();
        Observable<WriteResult>
                writeResultO = new FirebaseDatabaseManager(newKeyRef)
                .data()
                .setValue(data);

        Flowable<WriteResult> writeResultFlow = RxJavaInterop
                .toV2Flowable(writeResultO);
        Flowable<WriteResponse> payloadFlow = writeResultFlow
                .map(writeResult -> writeResponse(path, newKeyRef));

        return payloadFlow;
    }

    private WriteResponse writeResponse(Path path,
                                        DatabaseReference newKeyRef) {
        return new WriteResponse(newKeyRef.getKey(),
                path.getChildPaths());
    }
}
