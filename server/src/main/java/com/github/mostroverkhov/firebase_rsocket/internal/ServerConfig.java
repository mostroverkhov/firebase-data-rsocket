package com.github.mostroverkhov.firebase_rsocket.internal;

import com.github.mostroverkhov.firebase_rsocket.internal.auth.Authenticator;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.RequestHandlers;
import com.github.mostroverkhov.r2.core.DataCodec;
import io.rsocket.Closeable;
import io.rsocket.transport.ServerTransport;

/** Created by Maksym Ostroverkhov on 27.02.17. */
public class ServerConfig<T extends Closeable> {
  private final Authenticator authenticator;
  private final RequestHandlers handlers;
  private final PayloadConverter payloadConverter;
  private final ServerTransport<T> serverTransport;
  private final DataCodec dataCodec;

  public ServerConfig(
      RequestHandlers handlers,
      Authenticator authenticator,
      PayloadConverter payloadConverter,
      ServerTransport<T> serverTransport,
      DataCodec dataCodec) {
    this.handlers = handlers;
    this.authenticator = authenticator;
    this.payloadConverter = payloadConverter;
    this.serverTransport = serverTransport;
    this.dataCodec = dataCodec;
  }

  public Authenticator authenticator() {
    return authenticator;
  }

  public RequestHandlers handlers() {
    return handlers;
  }

  public PayloadConverter payloadConverter() {
    return payloadConverter;
  }

  public ServerTransport<T> serverTransport() {
    return serverTransport;
  }

  public DataCodec dataCodec() {
    return dataCodec;
  }
}
