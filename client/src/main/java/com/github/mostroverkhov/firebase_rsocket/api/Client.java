package com.github.mostroverkhov.firebase_rsocket.api;

import com.github.mostroverkhov.firebase_rsocket.Action;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NotifResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteResponse;
import io.reactivex.Flowable;

import static com.github.mostroverkhov.firebase_rsocket_data.common.model.Op.*;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface Client {

    @Action(DATA_WINDOW)
    Flowable<ReadResponse> dataWindow(ReadRequest readRequest);

    @Action(DATA_WINDOW_NOTIF)
    Flowable<NotifResponse> dataWindowNotifications(ReadRequest readRequest);

    @Action(WRITE_PUSH)
    Flowable<WriteResponse> write(WriteRequest<?> writeRequest);

    @Action(DELETE)
    Flowable<DeleteResponse> delete(DeleteRequest deleteRequest);
}
