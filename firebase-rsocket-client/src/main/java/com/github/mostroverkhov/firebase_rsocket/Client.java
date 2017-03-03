package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.gson.GsonUtil;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.DataWindow;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.ReadQuery;
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

    public static QueryBuilder query(String... childPaths) {
        return new QueryBuilder(childPaths);
    }

    public <T> Flowable<DataWindow<T>> dataWindow(ReadQuery readQuery, Class<T> clazz) {
        readQuery.setOperation(Op.DATA_WINDOW.code());

        SocketAddress address = clientConfig.getSocketAddress();
        Flowable<ReactiveSocket> socketFlow = Flowable.fromPublisher(
                ReactiveSocketClient.create(TcpTransportClient.create(address),
                        keepAlive(never()).disableLease())
                        .connect());

        Flowable<DataWindow<T>> dataWindowFlow = socketFlow
                .flatMap(socket -> socket
                        .requestStream(
                                toPayload(
                                        clientContext.gson(),
                                        readQuery)))
                .filter(requestStreamDataFrames())
                .map(payload -> toDataWindow(
                        clientContext.gson(),
                        payload,
                        clazz));

        return dataWindowFlow;
    }

    /*workaround for https://github.com/ReactiveSocket/reactivesocket-java/issues/226*/
    private static Predicate<Payload> requestStreamDataFrames() {
        return payload -> (payload instanceof Frame)
                && (((Frame) payload).getType() != FrameType.NEXT_COMPLETE);
    }

    private <T> DataWindow<T> toDataWindow(Gson gson,
                                           Payload payload,
                                           Class<T> itemType) {
        String dataStr = ByteBufferUtil.toUtf8String(payload.getData());
        return GsonUtil.parseDataWindow(gson, dataStr, itemType);
    }

    private Payload toPayload(Gson gson, ReadQuery readQuery) {
        return new PayloadImpl(gson.toJson(readQuery));
    }

}
