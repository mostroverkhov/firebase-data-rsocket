package com.github.mostroverkhov.firebase_rsocket.server.tcp.entrypoint;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

/** Created with IntelliJ IDEA. Author: mostroverkhov */
class ArtifactMetadataLoader {

  private final String metaDataProp;

  public ArtifactMetadataLoader(String metaDataProp) {
    this.metaDataProp = metaDataProp;
  }

  public Metadata metadata() {
    return getMetadata(metaDataProp);
  }

  private static Metadata getMetadata(String metadataProp) {
    Properties properties = new Properties();
    try {
      InputStream resourceAsStream =
          StandaloneServerEntryPoint.class.getClassLoader().getResourceAsStream(metadataProp);
      properties.load(resourceAsStream);
      Optional<String> version = Optional.ofNullable(properties.getProperty("server.version"));
      return new Metadata(version);
    } catch (IOException e) {
      throw new IllegalArgumentException(
          "Error reading metadata from file on classpath: " + metadataProp, e);
    }
  }

  public static class Metadata {
    private final Optional<String> version;

    public Metadata(Optional<String> version) {
      this.version = version;
    }

    public Optional<String> getVersion() {
      return version;
    }
  }
}
