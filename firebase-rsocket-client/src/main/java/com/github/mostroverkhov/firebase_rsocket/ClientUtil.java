package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class ClientUtil {

    static KeyValue metadata(String... tuples) {
        if (tuples.length % 2 == 0) {
            KeyValue metadata = new KeyValue();
            for (int i = 0; i < tuples.length - 1; i++) {
                metadata.put(tuples[i], tuples[i + 1]);
            }
            return metadata;
        } else {
            throw new IllegalArgumentException("Args should be even");
        }
    }
}
