package com.github.mostroverkhov.firebase_rsocket.api;

import com.github.mostroverkhov.firebase_rsocket.Action;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.delete.DeleteRequest;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.delete.DeleteResponse;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.notifications.NotifResponse;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.read.ReadResponse;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.write.WriteResponse;
import io.reactivex.Flowable;

import static com.github.mostroverkhov.firebase_rsocket.clientcommon.model.Op.*;

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
