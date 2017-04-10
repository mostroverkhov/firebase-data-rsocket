package com.github.mostroverkhov.firebase_rsocket_data;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class KeyValue {

    private final Map<String, Object> keyValues = new ConcurrentHashMap<>();

    public KeyValue put(String key, Object val) {
        keyValues.put(key, val);
        return this;
    }

    public Object get(String key) {
        return keyValues.get(key);
    }

    public KeyValue remove(String key) {
        keyValues.remove(key);
        return this;
    }

    public Object contains(String key) {
        return keyValues.containsKey(key);
    }

    public Set<String> keys() {
        return keyValues.keySet();
    }
}
