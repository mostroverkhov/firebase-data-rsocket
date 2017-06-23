package com.github.mostroverkhov.firebase_rsocket.api;

import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.Path;
import com.github.mostroverkhov.firebase_rsocket.clientcommon.model.read.ReadRequest;

import static com.github.mostroverkhov.firebase_rsocket.clientcommon.model.read.ReadRequest.OrderBy.CHILD;
import static com.github.mostroverkhov.firebase_rsocket.clientcommon.model.read.ReadRequest.OrderBy.KEY;
import static com.github.mostroverkhov.firebase_rsocket.clientcommon.model.read.ReadRequest.OrderBy.VALUE;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
public class ReadBuilder {

    private int windowSize = 25;
    private ReadRequest.OrderDir orderDir = ReadRequest.OrderDir.ASC;
    private ReadRequest.OrderBy orderBy = KEY;
    private String key;
    private String startWith;
    private final Path path;

    ReadBuilder(String... childPaths) {
        assertNotEmpty(childPaths);
        this.path = new Path(childPaths);

    }

    public ReadBuilder windowWithSize(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Size should be positive");
        }
        this.windowSize = size;
        return this;
    }

    public ReadBuilder orderByChild(String key) {
        assertNotNull(key);
        this.key = key;
        orderBy = CHILD;
        return this;
    }

    public ReadBuilder orderByKey() {
        orderBy = KEY;
        this.key = null;
        return this;
    }

    public ReadBuilder orderByValue() {
        orderBy = VALUE;
        this.key = null;
        return this;
    }

    public ReadBuilder asc() {
        orderDir = ReadRequest.OrderDir.ASC;
        return this;
    }

    public ReadBuilder desc() {
        orderDir = ReadRequest.OrderDir.DESC;
        return this;
    }

    public ReadBuilder startWith(String key) {
        assertNotNull(key);
        this.startWith = key;
        return this;
    }

    public ReadBuilder clearStartWith() {
        this.startWith = null;
        return this;
    }

    public ReadRequest build() {

        return new ReadRequest(
                path,
                windowSize,
                orderDir,
                orderBy,
                key,
                startWith);
    }

    static void assertNotNull(String key) {
        if (key == null) {
            throw new IllegalArgumentException("arg should not be null");
        }
    }

    static void assertNotEmpty(String[] dataPath) {
        if (dataPath == null || dataPath.length == 0) {
            throw new IllegalArgumentException("Data path should not be empty");
        }
    }

}
