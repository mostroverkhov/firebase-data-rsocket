package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.model.notifications.NotifResponse;
import com.github.mostroverkhov.firebase_rsocket.model.notifications.TypedNotifResponse;
import com.github.mostroverkhov.firebase_rsocket.model.read.ReadResponse;
import com.github.mostroverkhov.firebase_rsocket.model.read.TypedReadResponse;
import com.github.mostroverkhov.firebase_rsocket.typed.Typed;
import io.rsocket.transport.netty.server.CloseableChannel;
import java.time.Duration;
import java.util.function.Function;
import org.junit.After;
import org.junit.Before;
import reactor.core.publisher.Mono;

/** Created with IntelliJ IDEA. Author: mostroverkhov */
public class AbstractTest {
  private static final String SERVER_HOST = "127.0.0.1";
  private static final int SERVER_PORT = 8090;

  protected Mono<Void> serverStop;
  protected Client client;
  protected Function<ReadResponse, TypedReadResponse<Data>> dataWindowTransformer;
  protected Function<NotifResponse, TypedNotifResponse<Data>> notifTransformer;

  @Before
  public void setUp() throws Exception {
    Server<CloseableChannel> server =
        new ServerBuilder(SERVER_HOST, SERVER_PORT)
            .cacheReads()
            .classpathPropsAuth("creds.properties")
            .build();

    CloseableChannel closeable = server.start().block();
    serverStop = Mono.fromRunnable(closeable::dispose).then(closeable.onClose());

    client = new ClientBuilder("localhost", SERVER_PORT).build().block();

    Typed typed = client.typed();

    notifTransformer = typed.notificationsOf(Data.class);
    dataWindowTransformer = typed.dataWindowOf(Data.class);
  }

  @After
  public void tearDown() throws Exception {
    stopServer();
  }

  protected void stopServer() {
    if (serverStop != null) {
      serverStop.block();
      serverStop = null;
    }
  }

  protected void stopServerDelayed(long millis) {
    if (serverStop != null) {
      serverStop.delaySubscription(Duration.ofMillis(millis)).block();
      serverStop = null;
    }
  }
}
