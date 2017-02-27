package com.github.mostroverkhov.firebase_rsocket_server.auth;

/**
 * Created by Maksym Ostroverkhov on 27.02.17.
 */
public interface AuthFactory {

    Authenticator authenticator(Object... args);
}
