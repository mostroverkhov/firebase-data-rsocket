package com.github.mostroverkhov.firebase_rsocket.server.handler.impl;

import com.github.mostroverkhov.firebase_data_rxjava.rx.FirebaseDatabaseManager;
import com.github.mostroverkhov.firebase_data_rxjava.rx.model.WriteResult;
import com.github.mostroverkhov.firebase_rsocket.ServerSocketAcceptor;
import com.github.mostroverkhov.firebase_rsocket.server.handler.RequestHandler;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Path;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteResponse;
import com.google.firebase.database.DatabaseReference;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivesocket.Payload;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import rx.Observable;

import static com.github.mostroverkhov.firebase_rsocket.server.handler.impl.HandlerCommon.payload;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class WritePushHandler implements RequestHandler {

    @Override
    public boolean canHandle(Operation op) {
        return Op.WRITE_PUSH.code().equals(op.getOp());
    }

    @Override
    public Publisher<Payload> handle(ServerSocketAcceptor.SocketContext context, Operation op) {
        WriteRequest writeRequest = (WriteRequest) op;

        Path path = writeRequest.getPath();
        DatabaseReference dbRef = HandlerCommon.reference(path);
        DatabaseReference newKeyRef = dbRef.push();

        Object data = writeRequest.getData();
        Observable<WriteResult>
                writeResultO = new FirebaseDatabaseManager(newKeyRef)
                .data()
                .setValue(data);

        Flowable<WriteResult> writeResultFlow = RxJavaInterop
                .toV2Flowable(writeResultO);
        Flowable<Payload> payloadFlow = writeResultFlow
                .map(writeResult -> writeResponse(path, newKeyRef))
                .map(resp -> payload(context.gson(), resp));

        return payloadFlow;
    }

    private WriteResponse writeResponse(Path path,
                                        DatabaseReference newKeyRef) {
        return new WriteResponse(newKeyRef.getKey(),
                path.getChildPaths());
    }
}
