package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.typed.Typed;
import java.util.Objects;

public class Client {

  private final FirebaseService firebaseService;
  private final Typed typed;

  public Client(FirebaseService firebaseService, Typed typed) {
    assertArgs(firebaseService, typed);
    this.firebaseService = firebaseService;
    this.typed = typed;
  }

  public FirebaseService request() {
    return firebaseService;
  }

  public Typed typed() {
    return typed;
  }

  private static void assertArgs(FirebaseService svc, Typed typed) {
    assertNotNull(svc, "FirebaseService");
    assertNotNull(typed, "Typed");
  }

  private static void assertNotNull(Object obj, String msg) {
    Objects.requireNonNull(obj, String.format("%s: must be non-null", msg));
  }
}
