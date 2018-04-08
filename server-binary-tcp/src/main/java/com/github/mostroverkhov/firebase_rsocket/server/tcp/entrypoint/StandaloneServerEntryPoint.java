package com.github.mostroverkhov.firebase_rsocket.server.tcp.entrypoint;

import com.github.mostroverkhov.firebase_rsocket.Server;
import com.github.mostroverkhov.firebase_rsocket.ServerBuilder;
import io.rsocket.Closeable;
import io.rsocket.transport.netty.server.NettyContextCloseable;
import reactor.core.publisher.Mono;

/** Created with IntelliJ IDEA. Author: mostroverkhov */
public class StandaloneServerEntryPoint {

  private static final String SERVER_PROPERTIES = "server.properties";
  private static final String TRANSPORT = "TCP";

  public static void main(String[] args) {
    try {
      Configuration config = new ConfigurationReader().read(args);
      ArtifactMetadataLoader.Metadata metadata =
          new ArtifactMetadataLoader(SERVER_PROPERTIES).metadata();
      WelcomeScreen welcomeScreen = new WelcomeScreen(data(config, metadata));
      welcomeScreen.show();

      Mono<NettyContextCloseable> started = newServer(config).start();
      started.flatMap(Closeable::onClose).block();

    } catch (ArgsException e) {
      throw new IllegalStateException(e.getMessage(), e);
    }
  }

  private static Server<NettyContextCloseable> newServer(Configuration conf) {
    Integer serverPort = conf.getPort();
    String credsFile = conf.getCredsFile();

    return new ServerBuilder(serverPort).cacheReads().fileSystemPropsAuth(credsFile).build();
  }

  static WelcomeScreen.Data data(Configuration config, ArtifactMetadataLoader.Metadata metadata) {
    return new WelcomeScreen.Data(
        metadata.getVersion().orElse("unknown"), TRANSPORT, String.valueOf(config.getPort()));
  }
}
