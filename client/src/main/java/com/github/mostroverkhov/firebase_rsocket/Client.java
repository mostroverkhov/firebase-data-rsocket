package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NotifResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteResponse;
import io.reactivex.Flowable;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface Client {

    @Action(Op.DATA_WINDOW)
    Flowable<ReadResponse> dataWindow(ReadRequest readRequest);

    @Action(Op.DATA_WINDOW_NOTIF)
    Flowable<NotifResponse> dataWindowNotifications(ReadRequest readRequest);

    @Action(Op.WRITE_PUSH)
    Flowable<WriteResponse> write(WriteRequest<?> writeRequest);

    @Action(Op.DELETE)
    Flowable<DeleteResponse> delete(DeleteRequest deleteRequest);
}
