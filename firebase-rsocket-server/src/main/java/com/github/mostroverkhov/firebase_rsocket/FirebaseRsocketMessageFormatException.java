package com.github.mostroverkhov.firebase_rsocket;

/**
 * Created by Maksym Ostroverkhov on 01.03.17.
 */
public class FirebaseRsocketMessageFormatException extends RuntimeException {

    public FirebaseRsocketMessageFormatException() {
    }

    public FirebaseRsocketMessageFormatException(String message) {
        super(message);
    }

    public FirebaseRsocketMessageFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public FirebaseRsocketMessageFormatException(Throwable cause) {
        super(cause);
    }

    public FirebaseRsocketMessageFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
