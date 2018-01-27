package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.model.delete.DeleteRequest;
import com.github.mostroverkhov.firebase_rsocket.model.delete.DeleteResponse;
import com.github.mostroverkhov.firebase_rsocket.model.notifications.NotifResponse;
import com.github.mostroverkhov.firebase_rsocket.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket.model.read.ReadResponse;
import com.github.mostroverkhov.firebase_rsocket.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket.model.write.WriteResponse;
import reactor.core.publisher.Flux;

public interface FirebaseService {

    Flux<ReadResponse> dataWindow(ReadRequest readRequest);

    Flux<NotifResponse> dataWindowNotifications(ReadRequest readRequest);

    Flux<WriteResponse> write(WriteRequest writeRequest);

    Flux<DeleteResponse> delete(DeleteRequest deleteRequest);

}
