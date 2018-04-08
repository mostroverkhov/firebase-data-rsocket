package com.github.mostroverkhov.firebase_rsocket.internal.auth;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** Created by Maksym Ostroverkhov on 15.02.2017. */
public class CredentialsSource {

  private final CredentialsSupplier credsSupplier;
  private final AtomicReference<Credentials> credsCache = new AtomicReference<>();
  private CredentialsFactory credentialsFactory;

  public CredentialsSource(
      CredentialsSupplier credentialsSupplier, CredentialsFactory credentialsFactory) {
    assertArgs(credentialsSupplier, credentialsFactory);
    this.credentialsFactory = credentialsFactory;
    this.credsSupplier = credentialsSupplier;
  }

  public Mono<Credentials> getCreds() {

    return Mono.<Credentials>create(
            e -> {
              try {
                Credentials creds = setCredsOnceAndGet();
                e.success(creds);
              } catch (Exception ex) {
                e.error(
                    new FirebaseRsocketAuthException(
                        "Error while reading credentials file. " + ex.getMessage(), ex));
              }
            })
        .subscribeOn(Schedulers.elastic());
  }

  private Credentials setCredsOnceAndGet() {
    Credentials creds = credsCache.get();
    if (creds == null) {

      NonResolvedCredentials nonResolvedCreds =
          credentialsFactory.credentials(credsSupplier.credentialsRef());

      TaggedStream serviceFileStream =
          credsSupplier.serviceFileRef(nonResolvedCreds.getServiceFile());

      creds =
          new Credentials(
              nonResolvedCreds.getDbUrl(),
              nonResolvedCreds.getUserId(),
              serviceFileStream.getStream());
      credsCache.compareAndSet(null, creds);
    }
    return credsCache.get();
  }

  private static void assertArgs(Object... args) {
    for (Object arg : args) {
      if (arg == null) {
        throw new IllegalArgumentException("Args should not be null: " + Arrays.toString(args));
      }
    }
  }
}
