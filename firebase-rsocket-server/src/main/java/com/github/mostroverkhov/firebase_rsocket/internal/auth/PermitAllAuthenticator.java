package com.github.mostroverkhov.firebase_rsocket.internal.auth;

import io.reactivex.Completable;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class PermitAllAuthenticator implements Authenticator {

    @Override
    public Completable authenticate() {
        return Completable.complete();
    }
}
