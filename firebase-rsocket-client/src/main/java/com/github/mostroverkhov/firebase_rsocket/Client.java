package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.gson.GsonUtil;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteResponse;
import com.google.gson.*;
import io.reactivesocket.Frame;
import io.reactivesocket.FrameType;
import io.reactivesocket.Payload;
import io.reactivesocket.ReactiveSocket;
import io.reactivesocket.client.ReactiveSocketClient;
import io.reactivesocket.frame.ByteBufferUtil;
import io.reactivesocket.transport.tcp.client.TcpTransportClient;
import io.reactivesocket.util.PayloadImpl;
import io.reactivex.Flowable;
import io.reactivex.functions.Predicate;

import java.net.SocketAddress;

import static io.reactivesocket.client.KeepAliveProvider.never;
import static io.reactivesocket.client.SetupProvider.keepAlive;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
public class Client {
    private final ClientConfig clientConfig;
    private final ClientContext clientContext;

    public Client(ClientConfig clientConfig,
                  ClientContext clientContext) {
        this.clientConfig = clientConfig;
        this.clientContext = clientContext;
    }

    public static class Requests {

        public static ReadRequestBuilder readRequest(String... childPaths) {
            return new ReadRequestBuilder(childPaths);
        }

        public static <T> WriteRequestBuilder<T> writeRequest(String... childPaths) {
            return new WriteRequestBuilder<>(childPaths);
        }
    }

    public <T> Flowable<ReadResponse<T>> dataWindow(ReadRequest readRequest, Class<T> clazz) {
        return dataWindowFlow(readRequest, clazz);
    }

    public <T> Flowable<WriteResponse> write(WriteRequest<T> writeRequest) {
        Flowable<ReactiveSocket> socketFlow = rsocketFlow();
        Flowable<WriteResponse> writeResponseFlow = socketFlow
                .flatMap(socket -> socket
                        .requestStream(
                                toPayload(
                                        clientContext.gson(),
                                        writeRequest)))
                .filter(requestStreamDataFrames())
                .map(payload -> toWriteResponse(
                        clientContext.gson(),
                        payload));

        return writeResponseFlow;
    }

    private <T> Flowable<ReadResponse<T>> dataWindowFlow(
            ReadRequest readRequest,
            Class<T> clazz) {

        readRequest.setOp(Op.DATA_WINDOW.code());

        Flowable<ReactiveSocket> socketFlow = rsocketFlow();

        Flowable<ReadResponse<T>> readResponseFlow = socketFlow
                .flatMap(socket -> socket
                        .requestStream(
                                toPayload(
                                        clientContext.gson(),
                                        readRequest)))
                .filter(requestStreamDataFrames())
                .map(payload -> toReadResponse(
                        clientContext.gson(),
                        payload,
                        clazz));

        return readResponseFlow;
    }

    private Flowable<ReactiveSocket> rsocketFlow() {
        SocketAddress address = clientConfig.getSocketAddress();
        return Flowable.fromPublisher(
                ReactiveSocketClient.create(TcpTransportClient.create(address),
                        keepAlive(never()).disableLease())
                        .connect());
    }

    /*workaround for https://github.com/ReactiveSocket/reactivesocket-java/issues/226*/
    private static Predicate<Payload> requestStreamDataFrames() {
        return payload -> (payload instanceof Frame)
                && (((Frame) payload).getType() != FrameType.NEXT_COMPLETE);
    }

    private static <T> ReadResponse<T> toReadResponse(Gson gson,
                                                      Payload payload,
                                                      Class<T> itemType) {
        String dataStr = ByteBufferUtil.toUtf8String(payload.getData());
        return GsonUtil.mapReadResponse(gson, dataStr, itemType);
    }

    private static WriteResponse toWriteResponse(Gson gson,
                                                 Payload payload) {
        String dataStr = ByteBufferUtil.toUtf8String(payload.getData());
        return GsonUtil.mapWriteResponse(gson, dataStr);
    }

    private Payload toPayload(Gson gson, Object request) {
        return new PayloadImpl(gson.toJson(request));
    }

}
