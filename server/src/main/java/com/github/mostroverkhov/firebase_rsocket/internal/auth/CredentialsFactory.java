package com.github.mostroverkhov.firebase_rsocket.internal.auth;

/** Created with IntelliJ IDEA. Author: mostroverkhov */
public interface CredentialsFactory {

  NonResolvedCredentials credentials(TaggedStream credentialsRef);
}
