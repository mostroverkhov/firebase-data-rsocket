package com.github.mostroverkhov.firebase_rsocket.codec.gson;

import com.github.mostroverkhov.r2.core.DataCodec;
import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import org.jetbrains.annotations.NotNull;

public class GsonDataCodec implements DataCodec {

  private static final String prefix = "json";
  private static final Charset utf8 = Charset.forName("UTF-8");

  private final Gson gson;

  public GsonDataCodec(Gson gson) {
    assertArgs(gson);
    this.gson = gson;
  }

  @NotNull
  @Override
  public String getPrefix() {
    return prefix;
  }

  @NotNull
  @Override
  public <T> ByteBuffer encode(T t) {
    return ByteBuffer.wrap(gson.toJson(t).getBytes(Charset.forName("UTF-8")));
  }

  @Override
  public <T> T decode(ByteBuffer byteBuffer, Class<T> clazz) {
    InputStreamReader reader = new InputStreamReader(new ByteBufferInputStream(byteBuffer), utf8);
    return gson.fromJson(reader, clazz);
  }

  private static void assertArgs(Gson gson) {
    if (gson == null) {
      throw new IllegalArgumentException("Args must be non-null");
    }
  }
}
