package com.github.mostroverkhov.firebase_rsocket.requests;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public final class Req {
    private Req() {
    }

    public static ReadBuilder read(String... childPaths) {
        return new ReadBuilder(childPaths);
    }

    public static <T> WriteBuilder<T> write(String... childPaths) {
        return new WriteBuilder<>(childPaths);
    }

    public static DeleteBuilder delete(String... childPaths) {
        return new DeleteBuilder(childPaths);
    }
}
