package com.biteme.app.exception;

public class RoleVerificationException extends RuntimeException {

    public RoleVerificationException(String message) {
        super(message);
    }

    public RoleVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}