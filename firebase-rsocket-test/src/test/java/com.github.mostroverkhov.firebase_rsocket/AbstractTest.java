package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.auth.PropsCredentialsFactory;
import com.github.mostroverkhov.firebase_rsocket.auth.ServerAuthenticator;
import com.google.gson.Gson;
import io.reactivex.Completable;
import org.junit.After;
import org.junit.Before;

import java.net.InetSocketAddress;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class AbstractTest {

    protected Completable serverStop;
    protected Client client;

    @SuppressWarnings("Duplicates")
    @Before
    public void setUp() throws Exception {
        Gson gson = new Gson();

        InetSocketAddress socketAddress = new InetSocketAddress(8090);
        ServerConfig serverConfig = new ServerConfig(
                socketAddress,
                new ServerAuthenticator(
                        new PropsCredentialsFactory("creds.properties")));
        ServerContext serverContext = new ServerContext(gson);

        ClientConfig clientConfig = new ClientConfig(socketAddress);
        ClientContext clientContext = new ClientContext(gson);
        client = new Client(clientConfig, clientContext);
        Server server = new Server(serverConfig, serverContext);
        serverStop = server.start();
    }

    @After
    public void tearDown() throws Exception {
        stopServer();
    }

    private void stopServer() {
        if (serverStop != null) {
            serverStop.subscribe();
            serverStop = null;
        }
    }
}
