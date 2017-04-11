package com.github.mostroverkhov.firebase_rsocket_data;

import java.util.Map;
import java.util.Optional;
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

    public boolean contains(String key) {
        return keyValues.containsKey(key);
    }

    public boolean contains(String key, String value) {
        return Optional.ofNullable(keyValues.get(key))
                .map(v -> v.equals(value))
                .orElse(false);
    }

    public Set<String> keys() {
        return keyValues.keySet();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("KeyValue{");
        sb.append("keyValues=").append(keyValues);
        sb.append('}');
        return sb.toString();
    }
}
