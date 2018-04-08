package com.github.mostroverkhov.firebase_rsocket.internal.handler;

import com.github.mostroverkhov.firebase_rsocket.FirebaseServiceContract;
import com.github.mostroverkhov.firebase_rsocket.internal.PayloadConverter;
import com.github.mostroverkhov.firebase_rsocket.model.delete.DeleteRequest;
import com.github.mostroverkhov.firebase_rsocket.model.delete.DeleteResponse;
import com.github.mostroverkhov.firebase_rsocket.model.notifications.NotifResponse;
import com.github.mostroverkhov.firebase_rsocket.model.notifications.TypedNotifResponse;
import com.github.mostroverkhov.firebase_rsocket.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket.model.read.ReadResponse;
import com.github.mostroverkhov.firebase_rsocket.model.read.TypedReadResponse;
import com.github.mostroverkhov.firebase_rsocket.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket.model.write.WriteResponse;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;

public class ServiceHandler implements FirebaseServiceContract {

  private final RequestHandlers requestHandlers;
  private final PayloadConverter converter;

  public ServiceHandler(RequestHandlers requestHandlers, PayloadConverter converter) {
    this.requestHandlers = requestHandlers;
    this.converter = converter;
  }

  @Override
  public Flux<ReadResponse> dataWindow(ReadRequest readRequest) {
    return requestHandlers.dataWindowHandler().handle(readRequest).map(this::asReadResponse);
  }

  @Override
  public Flux<NotifResponse> dataWindowNotifications(ReadRequest readRequest) {
    return requestHandlers.notifHandler().handle(readRequest).map(this::asNotifResponse);
  }

  @Override
  public Flux<WriteResponse> write(WriteRequest writeRequest) {
    return requestHandlers.writePushHandler().handle(writeRequest);
  }

  @Override
  public Flux<DeleteResponse> delete(DeleteRequest deleteRequest) {
    return requestHandlers.deleteHandler().handle(deleteRequest);
  }

  @NotNull
  private ReadResponse asReadResponse(TypedReadResponse<?> resp) {
    return new ReadResponse(resp.getReadRequest(), converter.convert(resp.getData()));
  }

  @NotNull
  private NotifResponse asNotifResponse(TypedNotifResponse resp) {
    if (resp.isChangeEvent()) {
      return NotifResponse.changeEvent(resp.getKind(), converter.convert(resp.getItem()));
    } else {
      return NotifResponse.nextWindow(resp.getNextDataWindow());
    }
  }
}
