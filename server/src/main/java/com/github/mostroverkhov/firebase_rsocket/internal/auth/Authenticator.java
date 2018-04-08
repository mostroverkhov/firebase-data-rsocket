package com.github.mostroverkhov.firebase_rsocket.internal.auth;

import reactor.core.publisher.Mono;

/** Created by Maksym Ostroverkhov on 27.02.17. */
public interface Authenticator {

  Mono<Void> authenticate();
}
