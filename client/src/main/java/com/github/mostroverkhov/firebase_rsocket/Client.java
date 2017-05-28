package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.ClientCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.delete.DeleteClientCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.notification.NotificationClientCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.read.DataWindowClientCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.util.GsonSerializer;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.write.WritePushClientCodec;
import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NotifResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteResponse;
import io.reactivex.Flowable;

import static com.github.mostroverkhov.firebase_rsocket.ClientUtil.metadata;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
class Client {

    private final ClientFlow clientFlow;
    private GsonSerializer serializer;

    public Client(ClientConfig clientConfig) {
        this.clientFlow = new ClientFlow(clientConfig);
        this.serializer = clientConfig.serializer();
    }

    public Flowable<ReadResponse> dataWindow(ReadRequest readRequest) {
        ClientCodec<ReadRequest, ReadResponse> codec = new DataWindowClientCodec(serializer);

        Flowable<ReadResponse> request = clientFlow.request(
                codec,
                readRequest,
                metadata(Op.key(), Op.DATA_WINDOW.value()));

        return request;
    }

    public Flowable<NotifResponse> dataWindowNotifications(ReadRequest readRequest) {

        ClientCodec<ReadRequest, NotifResponse> codec = new NotificationClientCodec(serializer);
        KeyValue metadata = metadata(Op.key(), Op.DATA_WINDOW_NOTIF.value());

        Flowable<NotifResponse> request = clientFlow.request(
                codec,
                readRequest,
                metadata);

        return request;
    }

    public Flowable<WriteResponse> write(WriteRequest<?> writeRequest) {

        WritePushClientCodec writePushClientCodec = new WritePushClientCodec(serializer);
        KeyValue metadata = metadata(Op.key(), Op.WRITE_PUSH.value());

        return clientFlow.request(
                writePushClientCodec,
                writeRequest,
                metadata);
    }

    public Flowable<DeleteResponse> delete(DeleteRequest deleteRequest) {
        DeleteClientCodec deleteClientCodec = new DeleteClientCodec(serializer);
        KeyValue metadata = metadata(Op.key(), Op.DELETE.value());

        return clientFlow.request(
                deleteClientCodec,
                deleteRequest,
                metadata);
    }

}
