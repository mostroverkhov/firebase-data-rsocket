package com.github.mostroverkhov.firebase_rsocket.server.tcp.entrypoint;

import com.github.mostroverkhov.firebase_rsocket.Server;
import io.reactivex.Completable;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class StandaloneServerEntryPoint {

    public static void main(String[] args) {
        Configuration config = new ArgsReader().read(args);
        Server server = new StandaloneServerConfigurer().configure(config);
        server.start();
        ArtifactMetadataLoader.Metadata metadata = new ArtifactMetadataLoader("artifact.properties")
                .metadata();
        WelcomeScreen.Data data = data(config, metadata);
        WelcomeScreen screen = new WelcomeScreen(data);
        screen.show();
        Completable.never().blockingAwait();
    }

    private static WelcomeScreen.Data data(Configuration config, ArtifactMetadataLoader.Metadata metadata) {
        return new WelcomeScreen.Data(
                metadata.getVersion().orElse("unknown"),
                "TCP",
                config.getPort().orElse("unknown"));
    }
}
