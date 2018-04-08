package com.github.mostroverkhov.firebase_rsocket.internal.auth;

import java.io.InputStream;
import java.util.function.Supplier;

/** Created with IntelliJ IDEA. Author: mostroverkhov */
public class TaggedStream {
  private final Supplier<InputStream> stream;
  private final String desc;

  public TaggedStream(Supplier<InputStream> stream, String desc) {
    this.stream = stream;
    this.desc = desc;
  }

  public Supplier<InputStream> getStream() {
    return stream;
  }

  public String getDesc() {
    return desc;
  }
}
