package com.github.mostroverkhov.firebase_rsocket.internal.handler.delete;

import com.github.mostroverkhov.firebase_data_rxjava.rx.FirebaseDatabaseManager;
import com.github.mostroverkhov.firebase_data_rxjava.rx.model.WriteResult;
import com.github.mostroverkhov.firebase_rsocket.servercommon.KeyValue;
import com.github.mostroverkhov.firebase_rsocket.servercommon.model.Path;
import com.github.mostroverkhov.firebase_rsocket.servercommon.model.delete.DeleteRequest;
import com.github.mostroverkhov.firebase_rsocket.servercommon.model.delete.DeleteResponse;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.MetadataRequestHandler;
import com.google.firebase.database.DatabaseReference;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.Flowable;
import rx.Observable;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DeleteHandler extends MetadataRequestHandler<DeleteRequest, DeleteResponse> {

    public DeleteHandler(String key, String value) {
        super(key, value);
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
