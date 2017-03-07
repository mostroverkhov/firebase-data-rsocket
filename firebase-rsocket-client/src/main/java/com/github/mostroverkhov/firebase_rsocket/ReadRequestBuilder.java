package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Path;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;

import static com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest.OrderBy.*;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
public class ReadRequestBuilder {

    private String operation = "";
    private int windowSize = 25;
    private ReadRequest.OrderDir orderDir = ReadRequest.OrderDir.ASC;
    private ReadRequest.OrderBy orderBy = KEY;
    private String key;
    private final Path path;

    public ReadRequestBuilder(String... childPaths) {
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
        if (key == null) {
            throw new IllegalArgumentException("key should not be null");
        }
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

    public ReadRequest build() {

        return new ReadRequest(operation,
                path,
                windowSize,
                orderDir,
                orderBy,
                key, null);
    }

    static void assertNotEmpty(String dataPath) {
        if (dataPath == null || dataPath.isEmpty()) {
            throw new IllegalArgumentException("Data path should not be empty");
        }
    }

}
