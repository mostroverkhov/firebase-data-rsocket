package com.github.mostroverkhov.firebase_rsocket.transport.aeron;

import com.github.mostroverkhov.firebase_rsocket.transport.ClientTransport;
import io.reactivesocket.aeron.client.AeronTransportClient;
import io.reactivesocket.aeron.internal.Constants;
import io.reactivesocket.aeron.internal.DefaultAeronWrapper;
import io.reactivesocket.aeron.internal.SingleThreadedEventLoop;
import io.reactivesocket.aeron.internal.reactivestreams.AeronClientChannelConnector;
import io.reactivesocket.aeron.internal.reactivestreams.AeronSocketAddress;
import io.reactivesocket.transport.TransportClient;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class ClientTransportAeron implements ClientTransport {
    private final AeronSocketAddress aeronSocketAddress;

    public ClientTransportAeron(AeronSocketAddress aeronSocketAddress) {
        assertNotNull(aeronSocketAddress);
        this.aeronSocketAddress = aeronSocketAddress;
    }

    @Override
    public TransportClient client() {
        SingleThreadedEventLoop clientEventLoop = new SingleThreadedEventLoop("client");
        AeronClientChannelConnector connector = AeronClientChannelConnector.create(
                new DefaultAeronWrapper(),
                aeronSocketAddress,
                clientEventLoop);

        AeronClientChannelConnector.AeronClientConfig config =
                AeronClientChannelConnector
                        .AeronClientConfig.create(
                        aeronSocketAddress,
                        aeronSocketAddress,
                        Constants.CLIENT_STREAM_ID,
                        Constants.SERVER_STREAM_ID,
                        clientEventLoop);

        return new AeronTransportClient(connector, config);
    }

    private static void assertNotNull(Object... args) {
        if (args == null) {
            throw new IllegalArgumentException("Arg should not be null");
        }
    }
}
