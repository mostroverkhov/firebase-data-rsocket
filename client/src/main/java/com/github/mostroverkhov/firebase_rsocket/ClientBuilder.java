package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.codec.gson.GsonDataCodec;
import com.github.mostroverkhov.firebase_rsocket.typed.Typed;
import com.github.mostroverkhov.firebase_rsocket.typed.gson.GsonTyped;
import com.github.mostroverkhov.firebase_rsocket.typed.gson.JsonAsStringTypeAdapter;
import com.github.mostroverkhov.r2.core.DataCodec;
import com.github.mostroverkhov.r2.core.requester.RequesterFactory;
import com.github.mostroverkhov.r2.java.R2Client;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

/** Created with IntelliJ IDEA. Author: mostroverkhov */
public class ClientBuilder {

  private final String host;
  private final int port;

  public ClientBuilder(String host, int port) {
    assertArg(host, port);
    this.host = host;
    this.port = port;
  }

  public Mono<Client> build() {
    return new R2Client()
        .connectWith(RSocketFactory.connect())
        .configureRequester(b -> b.codec(Defaults.dataCodec))
        .transport(TcpClientTransport.create(host, port))
        .start()
        .map(this::createClient);
  }

  @NotNull
  private Client createClient(RequesterFactory f) {
    FirebaseService svc = f.create(FirebaseServiceContract.class);
    Typed typed = Defaults.typed;
    return new Client(svc, typed);
  }

  private void assertArg(String host, int port) {
    if (host == null || host.isEmpty()) {
      throw new IllegalArgumentException("host must be non-empty");
    }
    if (port <= 0) {
      throw new IllegalArgumentException("port must be positive number");
    }
  }

  private static class Defaults {
    private static final Gson gson =
        new GsonBuilder()
            .registerTypeAdapterFactory(
                new JsonAsStringTypeAdapter.JsonAsStringTypeAdapterFactory())
            .create();
    static final DataCodec dataCodec = new GsonDataCodec(gson);
    static final Typed typed = new GsonTyped(gson);
  }
}
