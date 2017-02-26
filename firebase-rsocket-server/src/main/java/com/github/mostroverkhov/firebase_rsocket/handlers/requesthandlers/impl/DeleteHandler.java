package com.github.mostroverkhov.firebase_rsocket.handlers.requesthandlers.impl;

import com.github.mostroverkhov.firebase_data_rxjava.rx.FirebaseDatabaseManager;
import com.github.mostroverkhov.firebase_data_rxjava.rx.model.WriteResult;
import com.github.mostroverkhov.firebase_rsocket.ServerSocketAcceptor;
import com.github.mostroverkhov.firebase_rsocket.handlers.requesthandlers.RequestHandler;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Path;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteResponse;
import com.google.firebase.database.DatabaseReference;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivesocket.Payload;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import rx.Observable;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DeleteHandler implements RequestHandler {

    @Override
    public boolean canHandle(Operation op) {
        return Op.DELETE.code().equals(op.getOp());
    }

    @Override
    public Publisher<Payload> handle(ServerSocketAcceptor.SocketContext context, Operation op) {
        DeleteRequest deleteRequest = (DeleteRequest) op;
        Path path = deleteRequest.getPath();
        DatabaseReference dbRef = HandlerCommon.reference(path);
        Observable<WriteResult> deleteO = new FirebaseDatabaseManager(dbRef)
                .data().removeValue();
        Flowable<WriteResult> deleteFlow = RxJavaInterop
                .toV2Flowable(deleteO);
        Flowable<Payload> payloadFlow = deleteFlow
                .map(writeResult -> deleteResponse(path))
                .map(resp -> HandlerCommon.payload(context.gson(), resp));

        return payloadFlow;
    }

    private DeleteResponse deleteResponse(Path path) {
        return new DeleteResponse(path.getChildPaths());
    }

}
