package com.github.mostroverkhov.firebase_rsocket_server.auth;

import io.reactivex.Completable;

/**
 * Created by Maksym Ostroverkhov on 27.02.17.
 */
public interface Authenticator {

    Completable authenticate();
}
