package com.github.mostroverkhov.firebase_rsocket.internal.handler;

import com.github.mostroverkhov.firebase_rsocket.FirebaseServiceContract;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.Authenticator;
import com.github.mostroverkhov.firebase_rsocket.model.delete.DeleteRequest;
import com.github.mostroverkhov.firebase_rsocket.model.delete.DeleteResponse;
import com.github.mostroverkhov.firebase_rsocket.model.notifications.NotifResponse;
import com.github.mostroverkhov.firebase_rsocket.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket.model.read.ReadResponse;
import com.github.mostroverkhov.firebase_rsocket.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket.model.write.WriteResponse;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public class AuthenticatingServiceHandler implements FirebaseServiceContract {
  private final Authenticator authenticator;
  private final FirebaseServiceContract source;

  public AuthenticatingServiceHandler(FirebaseServiceContract source, Authenticator authenticator) {
    this.authenticator = authenticator;
    this.source = source;
  }

  @Override
  public Flux<ReadResponse> dataWindow(ReadRequest readRequest) {
    return authThen(source.dataWindow(readRequest));
  }

  @Override
  public Flux<NotifResponse> dataWindowNotifications(ReadRequest readRequest) {
    return authThen(source.dataWindowNotifications(readRequest));
  }

  @Override
  public Flux<WriteResponse> write(WriteRequest writeRequest) {
    return authThen(source.write(writeRequest));
  }

  @Override
  public Flux<DeleteResponse> delete(DeleteRequest deleteRequest) {
    return authThen(source.delete(deleteRequest));
  }

  private <T> Flux<T> authThen(Publisher<T> source) {
    return authenticator.authenticate().thenMany(source);
  }
}
