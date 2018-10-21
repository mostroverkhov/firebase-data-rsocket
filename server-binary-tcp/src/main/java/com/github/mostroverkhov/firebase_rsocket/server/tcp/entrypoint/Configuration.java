package com.github.mostroverkhov.firebase_rsocket.server.tcp.entrypoint;

import java.util.Objects;

/** Created with IntelliJ IDEA. Author: mostroverkhov */
class Configuration {
  private final Integer port;
  private final String credsFile;
  private final String bindAddress;

  public Configuration(String bindAddress, Integer port, String credsFile) {
    Objects.requireNonNull(bindAddress, "address");
    Objects.requireNonNull(port, "port");
    Objects.requireNonNull(credsFile, "credsFile");
    this.bindAddress = bindAddress;
    this.port = port;
    this.credsFile = credsFile;
  }

  public String getBindAddress() {
    return bindAddress;
  }

  public Integer getPort() {
    return port;
  }

  public String getCredsFile() {
    return credsFile;
  }
}
