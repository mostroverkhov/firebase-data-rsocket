package com.github.mostroverkhov.firebase_rsocket.internal.handler.impl.delete;

import com.github.mostroverkhov.firebase_data_rxjava.rx.FirebaseDatabaseManager;
import com.github.mostroverkhov.firebase_data_rxjava.rx.model.WriteResult;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.impl.OperationRequestHandler;
import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Path;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteResponse;
import com.google.firebase.database.DatabaseReference;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.Flowable;
import rx.Observable;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DeleteHandler extends OperationRequestHandler<DeleteRequest, DeleteResponse> {

    public DeleteHandler() {
        super(Op.DELETE);
    }

    @Override
    public Flowable<DeleteResponse> handle(KeyValue metadata, DeleteRequest deleteRequest) {
        Path path = deleteRequest.getPath();
        DatabaseReference dbRef = reference(path);
        Observable<WriteResult> deleteO = new FirebaseDatabaseManager(dbRef)
                .data().removeValue();
        Flowable<WriteResult> deleteFlow = RxJavaInterop
                .toV2Flowable(deleteO);
        Flowable<DeleteResponse> payloadFlow = deleteFlow
                .map(writeResult -> deleteResponse(path));

        return payloadFlow;
    }

    private DeleteResponse deleteResponse(Path path) {
        return new DeleteResponse(path.getChildPaths());
    }

}
