package com.github.mostroverkhov.firebase_rsocket_data.common.transport;

import io.reactivesocket.transport.TransportClient;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface ClientTransport {

    TransportClient transportClient();
}
