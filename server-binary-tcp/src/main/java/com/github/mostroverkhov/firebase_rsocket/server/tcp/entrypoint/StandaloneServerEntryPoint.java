package com.github.mostroverkhov.firebase_rsocket.server.tcp.entrypoint;

import com.github.mostroverkhov.firebase_rsocket.Server;
import io.reactivex.Completable;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class StandaloneServerEntryPoint {

    public static final String SERVER_PROPERTIES = "server.properties";

    public static void main(String[] args) {
        try {
            Configuration config = new ConfigurationReader().read(args);
            ArtifactMetadataLoader.Metadata metadata = new ArtifactMetadataLoader(
                    SERVER_PROPERTIES)
                    .metadata();
            WelcomeScreen.Data data = data(config, metadata);
            WelcomeScreen screen = new WelcomeScreen(data);
            screen.show();

            Server server = new StandaloneServerConfigurer().configure(config);
            server.start();
            Completable.never().blockingAwait();
        } catch (ArgsException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    static WelcomeScreen.Data data(Configuration config, ArtifactMetadataLoader.Metadata metadata) {
        return new WelcomeScreen.Data(
                metadata.getVersion().orElse("unknown"),
                "TCP",
                String.valueOf(config.getPort()));
    }
}
