package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.mapper.gson.DataWindowClientMapper;
import com.github.mostroverkhov.firebase_rsocket.internal.mapper.gson.DeleteClientMapper;
import com.github.mostroverkhov.firebase_rsocket.internal.mapper.gson.NotificationClientMapper;
import com.github.mostroverkhov.firebase_rsocket.internal.mapper.gson.WritePushClientMapper;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NotifResponse;
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

    private final ClientRequestChain clientChain;

    public Client(ClientConfig clientConfig) {
        this.clientChain = new ClientRequestChain(clientConfig);
    }

    public <T> Flowable<ReadResponse<T>> dataWindow(ReadRequest readRequest,
                                                    Class<T> clazz) {
        return clientChain.request(
                new DataWindowClientMapper<>(clazz),
                readRequest,
                metadata(Op.key(), Op.DATA_WINDOW.value()));
    }

    public <T> Flowable<NotifResponse> dataWindowNotifications(ReadRequest readRequest,
                                                               Class<T> clazz) {
        return clientChain.request(
                new NotificationClientMapper<>(clazz),
                readRequest,
                metadata(Op.key(), Op.DATA_WINDOW_NOTIF.value()));
    }

    public <T> Flowable<WriteResponse> write(WriteRequest<T> writeRequest) {
        return clientChain.request(
                new WritePushClientMapper<>(),
                writeRequest,
                metadata(Op.key(), Op.WRITE_PUSH.value()));
    }

    public Flowable<DeleteResponse> delete(DeleteRequest deleteRequest) {
        return clientChain.request(
                new DeleteClientMapper(),
                deleteRequest,
                metadata(Op.key(), Op.DELETE.value()));
    }

}
