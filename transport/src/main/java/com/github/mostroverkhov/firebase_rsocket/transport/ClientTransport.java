package com.github.mostroverkhov.firebase_rsocket.transport;

import io.reactivesocket.transport.TransportClient;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface ClientTransport {

    TransportClient client();
}
