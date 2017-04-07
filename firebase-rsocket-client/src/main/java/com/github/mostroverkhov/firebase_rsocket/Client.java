package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.mapper.*;
import com.github.mostroverkhov.firebase_rsocket_data.common.Conversions;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NotifResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteResponse;
import io.reactivesocket.Frame;
import io.reactivesocket.FrameType;
import io.reactivesocket.Payload;
import io.reactivesocket.ReactiveSocket;
import io.reactivesocket.client.ReactiveSocketClient;
import io.reactivex.Flowable;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Publisher;

import static io.reactivesocket.client.KeepAliveProvider.never;
import static io.reactivesocket.client.SetupProvider.keepAlive;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
class Client {
    private final ClientConfig clientConfig;
    private final Flowable<ReactiveSocket> rsocket;

    public Client(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        this.rsocket = rsocket();
    }

    public <T> Flowable<ReadResponse<T>> dataWindow(ReadRequest readRequest,
                                                    Class<T> clazz) {
        return dataWindowFlow(readRequest, clazz);
    }

    public <T> Flowable<NotifResponse> dataWindowNotifications(ReadRequest readRequest,
                                                               Class<T> clazz) {
        return dataWindowNotificationsFlow(readRequest, clazz);
    }

    public <T> Flowable<WriteResponse> write(WriteRequest<T> writeRequest) {
        return writeFlow(writeRequest);
    }

    public Flowable<DeleteResponse> delete(DeleteRequest deleteRequest) {
        return deleteFlow(deleteRequest);
    }

    private Flowable<DeleteResponse> deleteFlow(DeleteRequest deleteRequest) {
        DeleteClientMapper deleteReqResp = new DeleteClientMapper(
                clientConfig.gson());
        return requestResponseFlow(deleteReqResp, deleteRequest);
    }

    private <T> Flowable<WriteResponse> writeFlow(WriteRequest<T> writeRequest) {
        WritePushClientMapper<T> reqResp = new WritePushClientMapper<>(
                clientConfig.gson());
        return requestResponseFlow(reqResp, writeRequest);
    }

    private <T> Flowable<ReadResponse<T>> dataWindowFlow(ReadRequest readRequest,
                                                         Class<T> clazz) {
        DataWindowClientMapper<T> reqResp = new DataWindowClientMapper<>(
                clientConfig.gson(),
                clazz);
        return requestResponseFlow(reqResp, readRequest);
    }

    private <T> Flowable<NotifResponse> dataWindowNotificationsFlow(ReadRequest readRequest,
                                                                    Class<T> clazz) {
        NotificationClientMapper<T> reqResp = new NotificationClientMapper<>(
                clientConfig.gson(),
                clazz);
        return requestResponseFlow(reqResp, readRequest);
    }

    private <Req extends Operation, Resp> Flowable<Resp> requestResponseFlow(
            ClientMapper<Req, Resp> clientMapper, Req request) {

        Flowable<Resp> readResponseFlow = rsocket
                .observeOn(Schedulers.io())
                .flatMap(socket -> {
                    Payload requestPayload = Conversions.bytesToPayload(
                            clientMapper.marshall(request));
                    return socket.requestStream(requestPayload);
                })
                .filter(requestStreamDataFrames())
                .flatMap(response -> {
                    Publisher<Resp> responseFlow = clientMapper
                            .map(Conversions.payloadToBytes(response));
                    return responseFlow;
                });

        return readResponseFlow;
    }

    private Flowable<ReactiveSocket> rsocket() {
        return Flowable.fromPublisher(
                ReactiveSocketClient.create(clientConfig.transport().transportClient(),
                        keepAlive(never()).disableLease())
                        .connect());
    }

    /*workaround for https://github.com/ReactiveSocket/reactivesocket-java/issues/226*/
    private static Predicate<Payload> requestStreamDataFrames() {
        return payload -> (payload instanceof Frame)
                && (((Frame) payload).getType() != FrameType.NEXT_COMPLETE);
    }
}
