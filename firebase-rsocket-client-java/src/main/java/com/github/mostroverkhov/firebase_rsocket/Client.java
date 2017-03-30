package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.request.DataWindowMarshallMap;
import com.github.mostroverkhov.firebase_rsocket.request.DeleteMarshallMap;
import com.github.mostroverkhov.firebase_rsocket.request.MarshallMap;
import com.github.mostroverkhov.firebase_rsocket.request.WritePushMarshallMap;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteResponse;
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

import static io.reactivesocket.client.KeepAliveProvider.never;
import static io.reactivesocket.client.SetupProvider.keepAlive;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
class Client {
    private final ClientConfig clientConfig;

    public Client(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    public <T> Flowable<ReadResponse<T>> dataWindow(ReadRequest readRequest,
                                                    Class<T> clazz) {
        return dataWindowFlow(readRequest, clazz);
    }

    public <T> Flowable<WriteResponse> write(WriteRequest<T> writeRequest) {
        return writeFlow(writeRequest);
    }

    public Flowable<DeleteResponse> delete(DeleteRequest deleteRequest) {
        return deleteFlow(deleteRequest);
    }

    private Flowable<DeleteResponse> deleteFlow(DeleteRequest deleteRequest) {
        DeleteMarshallMap deleteReqResp = new DeleteMarshallMap(
                clientConfig.gson());
        return requestResponseFlow(deleteReqResp, deleteRequest);
    }

    private <T> Flowable<WriteResponse> writeFlow(WriteRequest<T> writeRequest) {
        WritePushMarshallMap<T> reqResp = new WritePushMarshallMap<>(
                clientConfig.gson());
        return requestResponseFlow(reqResp, writeRequest);
    }

    private <T> Flowable<ReadResponse<T>> dataWindowFlow(ReadRequest readRequest,
                                                         Class<T> clazz) {
        DataWindowMarshallMap<T> reqResp = new DataWindowMarshallMap<>(
                clientConfig.gson(),
                clazz);
        return requestResponseFlow(reqResp, readRequest);
    }

    private <Req, Resp> Flowable<Resp> requestResponseFlow(
            MarshallMap<Req, Resp> requestResponse,
            Req request) {

        Flowable<Resp> readResponseFlow = rsocket()
                .flatMap(socket -> socket
                        .requestStream(requestResponse.marshallRequest(request)))
                .filter(requestStreamDataFrames())
                .flatMap(requestResponse::mapResponse);

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
