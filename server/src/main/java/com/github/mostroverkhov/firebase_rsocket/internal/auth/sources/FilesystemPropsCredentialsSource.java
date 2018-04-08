package com.github.mostroverkhov.firebase_rsocket.internal.auth.sources;

import com.github.mostroverkhov.firebase_rsocket.internal.auth.CredentialsSource;

/** Created with IntelliJ IDEA. Author: mostroverkhov */
public class FilesystemPropsCredentialsSource extends CredentialsSource {
  public FilesystemPropsCredentialsSource(String fsFileName) {
    super(new FilesystemCredentialsSupplier(fsFileName), new PropCredentialsFactory());
  }
}
