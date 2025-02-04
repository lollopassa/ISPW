package com.biteme.app.exception;

public class ServerInitializationException extends Exception {
    public ServerInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerInitializationException(String message) {
        super(message);
    }
}