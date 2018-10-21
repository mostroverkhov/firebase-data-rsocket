package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.PayloadConverter;
import com.github.mostroverkhov.firebase_rsocket.internal.ServerConfig;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.Authenticator;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.AuthenticatingServiceHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.RequestHandlers;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.ServiceHandler;
import com.github.mostroverkhov.r2.core.Codecs;
import com.github.mostroverkhov.r2.core.DataCodec;
import com.github.mostroverkhov.r2.core.Services;
import com.github.mostroverkhov.r2.reactor.R2Server;
import com.github.mostroverkhov.r2.reactor.ServerAcceptorBuilder;
import io.rsocket.Closeable;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.ServerTransport;
import reactor.core.publisher.Mono;

/** Created by Maksym Ostroverkhov on 27.02.17. */
public class Server<T extends Closeable> {

  private final Authenticator authenticator;
  private final ServerTransport<T> transport;
  private final RequestHandlers requestHandlers;
  private final PayloadConverter payloadConverter;
  private final DataCodec dataCodec;

  Server(ServerConfig<T> serverConfig) {
    this.requestHandlers = serverConfig.handlers();
    this.authenticator = serverConfig.authenticator();
    this.payloadConverter = serverConfig.payloadConverter();
    this.transport = serverConfig.serverTransport();
    this.dataCodec = serverConfig.dataCodec();
  }

  public Mono<T> start() {
    return new R2Server<T>()
        .connectWith(RSocketFactory.receive())
        .configureAcceptor(this::configureAcceptor)
        .transport(transport)
        .start();
  }

  private ServerAcceptorBuilder configureAcceptor(ServerAcceptorBuilder builder) {
    return builder
        .codecs(new Codecs().add(dataCodec))
        .services(
            (ctx, requesterFactory) ->
                new Services()
                    .add(
                        new AuthenticatingServiceHandler(
                            new ServiceHandler(requestHandlers, payloadConverter), authenticator)));
  }
}
