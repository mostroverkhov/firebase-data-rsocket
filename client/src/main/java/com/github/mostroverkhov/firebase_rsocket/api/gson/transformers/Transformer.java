package com.github.mostroverkhov.firebase_rsocket.api.gson.transformers;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface Transformer<T, R> {

    R from(T t);
}
