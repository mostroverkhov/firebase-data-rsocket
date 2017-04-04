package com.github.mostroverkhov.firebase_rsocket_data.common.transport;

import io.reactivesocket.transport.TransportServer;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface ServerTransport {

    TransportServer transportServer();
}
