package com.github.mostroverkhov.firebase_rsocket.internal.handler;

import com.github.mostroverkhov.firebase_rsocket.internal.handler.delete.DeleteHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.read.DataWindowHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.read.NotifHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.read.cache.Cache;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.write.WritePushHandler;
import java.util.Optional;

public class RequestHandlers {
  private final DeleteHandler deleteHandler;
  private final WritePushHandler writePushHandler;
  private final DataWindowHandler dataWindowHandler;
  private final NotifHandler notifHandler;

  public RequestHandlers(Optional<Cache> cache) {
    this.deleteHandler = new DeleteHandler();
    this.writePushHandler = new WritePushHandler();
    this.dataWindowHandler = new DataWindowHandler(cache);
    this.notifHandler = new NotifHandler(cache);
  }

  public DeleteHandler deleteHandler() {
    return deleteHandler;
  }

  public WritePushHandler writePushHandler() {
    return writePushHandler;
  }

  public DataWindowHandler dataWindowHandler() {
    return dataWindowHandler;
  }

  public NotifHandler notifHandler() {
    return notifHandler;
  }
}
