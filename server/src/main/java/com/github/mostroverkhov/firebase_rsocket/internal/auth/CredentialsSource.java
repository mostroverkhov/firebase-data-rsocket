package com.github.mostroverkhov.firebase_rsocket.internal.auth;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Maksym Ostroverkhov on 15.02.2017.
 */

public class CredentialsSource {

    private final CredentialsSupplier credsSupplier;
    private final AtomicReference<Credentials> credsCache = new AtomicReference<>();
    private CredentialsFactory credentialsFactory;

    public CredentialsSource(CredentialsSupplier credentialsSupplier,
                             CredentialsFactory credentialsFactory) {
        assertArgs(credentialsSupplier, credentialsFactory);
        this.credentialsFactory = credentialsFactory;
        this.credsSupplier = credentialsSupplier;
    }

    public Single<Credentials> getCreds() {

        return Single.<Credentials>create(e -> {
            if (!e.isDisposed()) {
                try {
                    Credentials creds = setCredsOnceAndGet();
                    if (!e.isDisposed()) {
                        e.onSuccess(creds);
                    }
                } catch (Exception ex) {
                    if (!e.isDisposed()) {
                        e.onError(new FirebaseRsocketAuthException(
                                "Error while reading credentials file. " + ex.getMessage(),
                                ex));
                    }
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    private Credentials setCredsOnceAndGet() {
        Credentials creds = credsCache.get();
        if (creds == null) {

            NonResolvedCredentials nonResolvedCreds = credentialsFactory
                    .credentials(credsSupplier.credentialsRef());

            TaggedStream serviceFileStream = credsSupplier
                    .serviceFileRef(nonResolvedCreds.getServiceFile());

            creds = new Credentials(nonResolvedCreds.getDbUrl(),
                    nonResolvedCreds.getUserId(),
                    serviceFileStream.getStream());
            credsCache.compareAndSet(null, creds);
        }
        return credsCache.get();
    }

    static void assertArgs(Object... args) {
        for (Object arg : args) {
            if (arg == null) {
                throw new IllegalArgumentException("Args should not be null: " + Arrays.toString(args));
            }
        }
    }
}
