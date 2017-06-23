package com.github.mostroverkhov.firebase_rsocket.transport.aeron;

import com.github.mostroverkhov.firebase_rsocket.transport.ServerTransport;
import io.reactivesocket.aeron.internal.DefaultAeronWrapper;
import io.reactivesocket.aeron.internal.SingleThreadedEventLoop;
import io.reactivesocket.aeron.internal.reactivestreams.AeronSocketAddress;
import io.reactivesocket.aeron.server.AeronTransportServer;
import io.reactivesocket.transport.TransportServer;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class ServerTransportAeron implements ServerTransport {

    private final AeronSocketAddress aeronSocketAddress;

    public ServerTransportAeron(AeronSocketAddress aeronSocketAddress) {
        assertAddress(aeronSocketAddress);
        this.aeronSocketAddress = aeronSocketAddress;
    }

    @Override
    public TransportServer transportServer() {

        return new AeronTransportServer(new DefaultAeronWrapper(),
                aeronSocketAddress,
                new SingleThreadedEventLoop("server"));

    }

    private  static void assertAddress(AeronSocketAddress aeronSocketAddress) {
        if (aeronSocketAddress == null) {
            throw new IllegalArgumentException("AeronSocketAddress should not be null");
        }
    }
}
