package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.ClientCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.GsonClientCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.delete.DeleteClientCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.notification.NotificationClientCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.notification.NotificationTransformer;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.read.DataWindowClientCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.read.DataWindowTransformer;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.util.GsonSerializer;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.write.WritePushClientCodec;
import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NonTypedNotificationResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NotificationResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.NonTypedReadResponse;
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

    private final ClientFlow clientChain;
    private GsonSerializer serializer;

    public Client(ClientConfig clientConfig) {
        this.clientChain = new ClientFlow(clientConfig);
        this.serializer = clientConfig.serializer();
    }

    public <T> Flowable<ReadResponse<T>> dataWindow(ReadRequest readRequest,
                                                    Class<T> windowItemType) {
        ClientCodec<ReadRequest, NonTypedReadResponse> codec = new DataWindowClientCodec(serializer);
        DataWindowTransformer<T> transformer = new DataWindowTransformer<>(serializer, windowItemType);

        Flowable<ReadResponse<T>> request = clientChain.request(
                codec,
                transformer::apply,
                readRequest,
                metadata(Op.key(), Op.DATA_WINDOW.value()));

        return request;
    }

    public <T> Flowable<NotificationResponse<T>> dataWindowNotifications(ReadRequest readRequest,
                                                                         Class<T> notificationItemType) {

        ClientCodec<ReadRequest, NonTypedNotificationResponse> codec = new NotificationClientCodec(serializer);
        NotificationTransformer<T> transformer = new NotificationTransformer<>(serializer, notificationItemType);
        KeyValue metadata = metadata(Op.key(), Op.DATA_WINDOW_NOTIF.value());

        Flowable<NotificationResponse<T>> request = clientChain.request(
                codec,
                transformer::apply,
                readRequest,
                metadata);

        return request;
    }

    public <T> Flowable<WriteResponse> write(WriteRequest<T> writeRequest) {

        WritePushClientCodec<T> writePushClientCodec = new WritePushClientCodec<>(serializer);
        KeyValue metadata = metadata(Op.key(), Op.WRITE_PUSH.value());

        return clientChain.request(
                writePushClientCodec,
                writeRequest,
                metadata);
    }

    public Flowable<DeleteResponse> delete(DeleteRequest deleteRequest) {
        DeleteClientCodec deleteClientCodec = new DeleteClientCodec(serializer);
        KeyValue metadata = metadata(Op.key(), Op.DELETE.value());

        return clientChain.request(
                deleteClientCodec,
                deleteRequest,
                metadata);
    }

}
