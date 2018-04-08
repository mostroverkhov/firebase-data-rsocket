package com.github.mostroverkhov.firebase_rsocket.internal.auth;

/** Created by Maksym Ostroverkhov on 28.02.17. */
public class FirebaseRsocketAuthException extends RuntimeException {

  public FirebaseRsocketAuthException() {}

  public FirebaseRsocketAuthException(String message) {
    super(message);
  }

  public FirebaseRsocketAuthException(String message, Throwable cause) {
    super(message, cause);
  }

  public FirebaseRsocketAuthException(Throwable cause) {
    super(cause);
  }
}
