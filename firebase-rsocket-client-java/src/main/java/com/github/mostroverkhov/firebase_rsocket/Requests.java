package com.github.mostroverkhov.firebase_rsocket;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public final class Requests {
    private Requests() {
    }

    public static ReadRequestBuilder readRequest(String... childPaths) {
        return new ReadRequestBuilder(childPaths);
    }

    public static <T> WriteRequestBuilder<T> writeRequest(String... childPaths) {
        return new WriteRequestBuilder<>(childPaths);
    }

    public static DeleteRequestBuilder deleteRequest(String... childPaths) {
        return new DeleteRequestBuilder(childPaths);
    }
}
