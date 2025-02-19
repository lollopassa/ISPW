package com.biteme.app.exception;

public class OrdineException extends Exception {

    public OrdineException(String message) {
        super(message);
    }

    public OrdineException(String message, Throwable cause) {
        super(message, cause);
    }

}