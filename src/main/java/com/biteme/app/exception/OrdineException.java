package com.biteme.app.exception;

public class OrdineException extends RuntimeException {

    public OrdineException(String message) {
        super(message);
    }

    public OrdineException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrdineException(Throwable cause) {
        super(cause);
    }
}
