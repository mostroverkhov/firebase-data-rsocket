package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Path;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;

import static com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest.OrderBy.*;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
public class ReadRequestBuilder {

    private String operation = Op.DATA_WINDOW.code();
    private int windowSize = 25;
    private ReadRequest.OrderDir orderDir = ReadRequest.OrderDir.ASC;
    private ReadRequest.OrderBy orderBy = KEY;
    private String key;
    private String startWith;
    private final Path path;

    ReadRequestBuilder(String... childPaths) {
        assertNotEmpty(childPaths);
        this.path = new Path(childPaths);

    }

    public ReadRequestBuilder windowWithSize(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Size should be positive");
        }
        this.windowSize = size;
        return this;
    }

    public ReadRequestBuilder orderByChild(String key) {
        assertNotNull(key);
        this.key = key;
        orderBy = CHILD;
        return this;
    }

    public ReadRequestBuilder orderByKey() {
        orderBy = KEY;
        this.key = null;
        return this;
    }

    public ReadRequestBuilder orderByValue() {
        orderBy = VALUE;
        this.key = null;
        return this;
    }

    public ReadRequestBuilder asc() {
        orderDir = ReadRequest.OrderDir.ASC;
        return this;
    }

    public ReadRequestBuilder desc() {
        orderDir = ReadRequest.OrderDir.DESC;
        return this;
    }

    public ReadRequestBuilder startWith(String key) {
        assertNotNull(key);
        this.startWith = key;
        return this;
    }

    public ReadRequestBuilder clearStartWith() {
        this.startWith = null;
        return this;
    }

    public ReadRequest build() {

        return new ReadRequest(operation,
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
