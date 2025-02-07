package com.biteme.app.exception;

public class GoogleAuthException extends Exception {
    // Costruttore che accetta solo il messaggio
    public GoogleAuthException(String message) {
        super(message);
    }

    // Costruttore che accetta il messaggio e la causa
    public GoogleAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}