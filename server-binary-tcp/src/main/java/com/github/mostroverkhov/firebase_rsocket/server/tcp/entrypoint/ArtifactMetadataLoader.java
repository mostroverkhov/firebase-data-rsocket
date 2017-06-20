package com.github.mostroverkhov.firebase_rsocket.server.tcp.entrypoint;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
class ArtifactMetadataLoader {

    private final String metaDataProp;

    public ArtifactMetadataLoader(String metaDataProp) {
        this.metaDataProp = metaDataProp;
    }

    public Metadata metadata() {
        return new Metadata(getServerVersion(metaDataProp));
    }

    private static Optional<String> getServerVersion(String metadataProp) {
        Properties properties = new Properties();
        try {
            InputStream resourceAsStream = StandaloneServerEntryPoint.class.getClassLoader()
                    .getResourceAsStream(metadataProp);
            properties.load(resourceAsStream);
            return Optional.of(properties.getProperty("server.version"));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static class Metadata {
        private final Optional<String> version;

        public Metadata(Optional<String> version) {
            this.version = version;
        }

        public Optional<String> getVersion() {
            return version;
        }
    }
}
