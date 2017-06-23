package com.github.mostroverkhov.firebase_rsocket.api;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface Transformer<T, R> {

    R from(T t);
}
