package com.github.mostroverkhov.firebase_rsocket.internal.auth;

import io.reactivex.Single;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
public interface CredentialsFactory {

    Single<Credentials> getCreds();
}
