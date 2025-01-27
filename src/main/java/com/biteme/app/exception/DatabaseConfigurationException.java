package com.biteme.app.exception;

public class DatabaseConfigurationException extends RuntimeException {
    public DatabaseConfigurationException(String message) {
        super(message);
    }

    public DatabaseConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}