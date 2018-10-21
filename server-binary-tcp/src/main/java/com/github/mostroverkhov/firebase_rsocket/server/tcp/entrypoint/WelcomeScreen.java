package com.github.mostroverkhov.firebase_rsocket.server.tcp.entrypoint;

import java.util.Date;

/** Created with IntelliJ IDEA. Author: mostroverkhov */
class WelcomeScreen {
  private final Data data;

  public WelcomeScreen(Data data) {
    this.data = data;
  }

  public void show() {
    System.out.println("\nFirebase-rsocket-server\n");
    System.out.println("Version: " + data.getVersion());
    System.out.println("Transport: " + data.getTransport());
    System.out.println("Address: " + data.getHost() + ":" + data.getPort());
    System.out.println("Started at " + new Date());
  }

  public static class Data {
    private final String version;
    private final String transport;
    private String host;
    private final String port;

    public Data(String version, String transport, String host, String port) {
      this.version = version;
      this.transport = transport;
      this.host = host;
      this.port = port;
    }

    public String getVersion() {
      return version;
    }

    public String getTransport() {
      return transport;
    }

    public String getHost() {
      return host;
    }

    public String getPort() {
      return port;
    }
  }
}
