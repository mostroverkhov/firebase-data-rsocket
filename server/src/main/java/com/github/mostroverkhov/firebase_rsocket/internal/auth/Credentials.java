package com.github.mostroverkhov.firebase_rsocket.internal.auth;

import java.io.InputStream;
import java.util.function.Supplier;

/** Created by Maksym Ostroverkhov on 28.02.2017. */
public class Credentials {
  private final String dbUrl;
  private final String userId;
  private final Supplier<InputStream> serviceFile;

  public Credentials(String dbUrl, String userId, Supplier<InputStream> serviceFile) {
    this.dbUrl = dbUrl;
    this.userId = userId;
    this.serviceFile = serviceFile;
  }

  public String getDbUrl() {
    return dbUrl;
  }

  public String getUserId() {
    return userId;
  }

  public Supplier<InputStream> getServiceFile() {
    return serviceFile;
  }
}
