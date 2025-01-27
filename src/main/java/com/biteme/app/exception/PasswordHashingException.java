package com.biteme.app.exception;

public class PasswordHashingException extends RuntimeException {  // Or extend a checked exception if you prefer

    public PasswordHashingException(String message) {
        super(message);
    }

    public PasswordHashingException(String message, Throwable cause) {
        super(message, cause);
    }
}