package com.github.mostroverkhov.firebase_rsocket.internal.auth.sources;

import com.github.mostroverkhov.firebase_rsocket.internal.auth.CredentialsSupplier;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.TaggedStream;

import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class ClasspathCredentialsSupplier implements CredentialsSupplier {

    private final String fileName;

    public ClasspathCredentialsSupplier(String fileName) {
        assertFilename(fileName);
        this.fileName = fileName;
    }

    @Override
    public TaggedStream credentialsRef() {
        return new TaggedStream(() -> read(fileName),
                String.format(
                        "Credentials file on classpath: %s",
                        fileName));

    }

    @Override
    public TaggedStream serviceFileRef(String ref) {
        return new TaggedStream(() -> read(ref),
                String.format(
                        "Service file on classpath: %s",
                        ref));

    }

    InputStream read(String fileName) {
        InputStream propsStream = getClass()
                .getClassLoader().getResourceAsStream(fileName);
        if (propsStream == null) {
            throw new IllegalStateException(
                    String.format(
                            "Error while reading file on classpath: %s",
                            fileName));
        }
        return propsStream;
    }

    private static void assertFilename(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("FileName should not be null");
        }
    }
}
