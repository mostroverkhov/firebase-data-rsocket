package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.codec.gson.GsonDataCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.GsonPayloadConverter;
import com.github.mostroverkhov.firebase_rsocket.internal.PayloadConverter;
import com.github.mostroverkhov.firebase_rsocket.internal.ServerConfig;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.Authenticator;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.CredentialsAuthenticator;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.sources.ClasspathPropsCredentialsSource;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.sources.FilesystemPropsCredentialsSource;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.RequestHandlers;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.read.cache.*;
import com.github.mostroverkhov.r2.core.DataCodec;
import com.google.gson.Gson;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.TcpServerTransport;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Created with IntelliJ IDEA. Author: mostroverkhov */
public class ServerBuilder {
  private Authenticator authenticator;
  private Optional<Cache> cache = Optional.empty();
  private final String host;
  private final int port;

  public ServerBuilder(String host, int port) {
    assertHost(host);
    assertPort(port);
    this.host = host;
    this.port = port;
  }

  public ServerBuilder classpathPropsAuth(String credsFile) {
    assertCredsFile(credsFile);
    this.authenticator =
        new CredentialsAuthenticator(new ClasspathPropsCredentialsSource(credsFile));
    return this;
  }

  public ServerBuilder fileSystemPropsAuth(String credsFile) {
    assertCredsFile(credsFile);
    this.authenticator =
        new CredentialsAuthenticator(new FilesystemPropsCredentialsSource(credsFile));
    return this;
  }

  public ServerBuilder cacheReads() {
    cache = Optional.of(Defaults.defaultCache);
    return this;
  }

  public ServerBuilder cacheReads(NativeCache nativeCache, CacheDuration cacheDuration) {
    assertNotNull(nativeCache, cacheDuration);
    cache = Optional.of(new Cache(nativeCache, cacheDuration));
    return this;
  }

  public ServerBuilder noCacheReads() {
    cache = Optional.empty();
    return this;
  }

  public Server<CloseableChannel> build() {
    validateState();

    RequestHandlers handlers = new RequestHandlers(cache);
    TcpServerTransport transport = TcpServerTransport.create(host, port);

    ServerConfig<CloseableChannel> serverConfig =
        new ServerConfig<>(
            handlers, authenticator, Defaults.payloadConverter, transport, Defaults.dataCodec);
    return new Server<>(serverConfig);
  }

  private void validateState() {
    if (authenticator == null) {
      throw new IllegalArgumentException("Authenticator must be set");
    }
  }

  private static void assertPort(int port) {
    if (port <= 0) {
      throw new IllegalArgumentException("Port must have positive value");
    }
  }

  private static void assertHost(String host) {
    if (host == null || host.isEmpty()) {
      throw new IllegalArgumentException("Bind address must be non-empty");
    }
  }

  private static void assertCredsFile(String credsFile) {
    if (credsFile == null || credsFile.isEmpty()) {
      throw new IllegalArgumentException("Credentials file should be present");
    }
  }

  private static void assertNotNull(Object... args) {
    for (Object arg : args) {
      if (arg == null) {
        throw new IllegalArgumentException("Args should not be null: " + Arrays.toString(args));
      }
    }
  }

  private static class Defaults {
    static final Gson gson = new Gson();
    static final PayloadConverter payloadConverter = new GsonPayloadConverter(gson);
    static final DataCodec dataCodec = new GsonDataCodec(gson);

    static final Cache defaultCache =
        new Cache(
            new NonconditionalCache(
                Executors.newSingleThreadScheduledExecutor(Defaults::newDaemonThread)),
            new CacheDurationConstant(5, TimeUnit.SECONDS));

    static Thread newDaemonThread(Runnable r) {
      Thread thread = new Thread(r);
      thread.setDaemon(true);
      return thread;
    }
  }
}
