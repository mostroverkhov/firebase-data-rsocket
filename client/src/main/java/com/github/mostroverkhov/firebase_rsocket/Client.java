package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.DataWindowClientCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.DeleteClientCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.NotificationClientCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.WritePushClientCodec;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NotificationResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteResponse;
import io.reactivex.Flowable;

import static com.github.mostroverkhov.firebase_rsocket.ClientUtil.metadata;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
class Client {

    private final ClientChain clientChain;

    public Client(ClientConfig clientConfig) {
        this.clientChain = new ClientChain(clientConfig);
    }

    public <T> Flowable<ReadResponse<T>> dataWindow(ReadRequest readRequest,
                                                    Class<T> clazz) {
        return clientChain.request(
                new DataWindowClientCodec<>(clazz),
                readRequest,
                metadata(Op.key(), Op.DATA_WINDOW.value()));
    }

    public <T> Flowable<NotificationResponse<T>> dataWindowNotifications(ReadRequest readRequest,
                                                                      Class<T> clazz) {
        return clientChain.request(
                new NotificationClientCodec<>(clazz),
                readRequest,
                metadata(Op.key(), Op.DATA_WINDOW_NOTIF.value()));
    }

    public <T> Flowable<WriteResponse> write(WriteRequest<T> writeRequest) {
        return clientChain.request(
                new WritePushClientCodec<>(),
                writeRequest,
                metadata(Op.key(), Op.WRITE_PUSH.value()));
    }

    public Flowable<DeleteResponse> delete(DeleteRequest deleteRequest) {
        return clientChain.request(
                new DeleteClientCodec(),
                deleteRequest,
                metadata(Op.key(), Op.DELETE.value()));
    }

}
