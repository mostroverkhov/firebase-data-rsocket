package com.github.mostroverkhov.firebase_rsocket;


import com.github.mostroverkhov.firebase_rsocket.model.delete.DeleteRequest;
import com.github.mostroverkhov.firebase_rsocket.model.delete.DeleteResponse;
import com.github.mostroverkhov.firebase_rsocket.model.notifications.NotifResponse;
import com.github.mostroverkhov.firebase_rsocket.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket.model.read.ReadResponse;
import com.github.mostroverkhov.firebase_rsocket.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket.model.write.WriteResponse;
import com.github.mostroverkhov.r2.core.contract.RequestStream;
import com.github.mostroverkhov.r2.core.contract.Service;
import reactor.core.publisher.Flux;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
@Service("firebase")
public interface FirebaseServiceContract extends FirebaseService {

    @RequestStream("window")
    Flux<ReadResponse> dataWindow(ReadRequest readRequest);

    @RequestStream("notifications")
    Flux<NotifResponse> dataWindowNotifications(ReadRequest readRequest);

    @RequestStream("write")
    Flux<WriteResponse> write(WriteRequest writeRequest);

    @RequestStream("delete")
    Flux<DeleteResponse> delete(DeleteRequest deleteRequest);
}
