package com.biteme.app.exception;

public class GoogleAuthException extends Exception {
    public GoogleAuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public GoogleAuthException(String message) {
        super(message);
    }
}