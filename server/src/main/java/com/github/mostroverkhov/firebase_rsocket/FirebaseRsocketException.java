package com.github.mostroverkhov.firebase_rsocket;

/**
 * Created by Maksym Ostroverkhov on 01.03.17.
 */
public class FirebaseRsocketException extends RuntimeException {

    public FirebaseRsocketException() {
    }

    public FirebaseRsocketException(String message) {
        super(message);
    }

    public FirebaseRsocketException(String message, Throwable cause) {
        super(message, cause);
    }

    public FirebaseRsocketException(Throwable cause) {
        super(cause);
    }

    public FirebaseRsocketException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
