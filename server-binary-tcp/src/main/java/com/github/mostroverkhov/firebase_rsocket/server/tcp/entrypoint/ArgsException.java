package com.github.mostroverkhov.firebase_rsocket.server.tcp.entrypoint;

public class ArgsException extends Exception {
  public ArgsException() {}

  public ArgsException(String message) {
    super(message);
  }

  public ArgsException(String message, Throwable cause) {
    super(message, cause);
  }
}
