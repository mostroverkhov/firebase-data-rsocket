package com.github.mostroverkhov.firebase_rsocket.internal.auth.sources;

import com.github.mostroverkhov.firebase_rsocket.internal.auth.CredentialsSupplier;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.TaggedStream;
import java.io.*;

/** Created with IntelliJ IDEA. Author: mostroverkhov */
public class FilesystemCredentialsSupplier implements CredentialsSupplier {
  private final String fsFileName;

  public FilesystemCredentialsSupplier(String fsFileName) {
    assertArgs(fsFileName);
    this.fsFileName = fsFileName;
  }

  @Override
  public TaggedStream credentialsRef() {
    return new TaggedStream(
        () -> readFile(fsFileName),
        String.format("Credentials file on file system: %s", fsFileName));
  }

  @Override
  public TaggedStream serviceFileRef(String ref) {
    return new TaggedStream(
        () -> readFile(ref), String.format("Service file on file system: %s", ref));
  }

  InputStream readFile(String fileName) {
    File file = new File(fileName);
    try {
      return new BufferedInputStream(new FileInputStream(file));
    } catch (FileNotFoundException e) {
      throw new IllegalStateException(
          String.format("Error while reading file on file system: \n %s", file.getAbsolutePath()),
          e);
    }
  }

  private void assertArgs(String fsFileName) {
    if (fsFileName == null) {
      throw new IllegalArgumentException("fsFileName should not be null");
    }
  }
}
