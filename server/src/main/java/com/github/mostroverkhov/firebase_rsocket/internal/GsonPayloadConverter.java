package com.github.mostroverkhov.firebase_rsocket.internal;

import com.google.gson.Gson;

public class GsonPayloadConverter implements PayloadConverter {
  private final Gson gson;

  public GsonPayloadConverter(Gson gson) {
    this.gson = gson;
  }

  @Override
  public String convert(Object t) {
    return gson.toJson(t);
  }
}
