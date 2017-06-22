package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.ClientCodec;
import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;
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

    private final ClientFlow clientFlow;
    private final ClientCodec codec;

    public Client(ClientConfig clientConfig) {
        this.clientFlow = new ClientFlow(clientConfig);
        this.codec = clientConfig.codec();
    }

    public Flowable<ReadResponse> dataWindow(ReadRequest readRequest) {

        KeyValue metadata = metadata(Op.key(), Op.DATA_WINDOW.value());
        Flowable<ReadResponse> request = clientFlow.request(
                codec,
                readRequest,
                ReadResponse.class,
                metadata);
        return request;
    }

    public Flowable<NotifResponse> dataWindowNotifications(ReadRequest readRequest) {

        KeyValue metadata = metadata(Op.key(), Op.DATA_WINDOW_NOTIF.value());
        Flowable<NotifResponse> request = clientFlow.request(
                codec,
                readRequest,
                NotifResponse.class,
                metadata);

        return request;
    }

    public Flowable<WriteResponse> write(WriteRequest<?> writeRequest) {

        KeyValue metadata = metadata(Op.key(), Op.WRITE_PUSH.value());
        return clientFlow.request(
                codec,
                writeRequest,
                WriteResponse.class,
                metadata);
    }

    public Flowable<DeleteResponse> delete(DeleteRequest deleteRequest) {
        KeyValue metadata = metadata(Op.key(), Op.DELETE.value());
        return clientFlow.request(
                codec,
                deleteRequest,
                DeleteResponse.class,
                metadata);
    }

}
