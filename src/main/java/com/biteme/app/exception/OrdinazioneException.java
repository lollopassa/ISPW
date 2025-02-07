package com.biteme.app.exception;

public class OrdinazioneException extends RuntimeException {

    public OrdinazioneException(String message) {
        super(message);
    }

    public OrdinazioneException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrdinazioneException(Throwable cause) {
        super(cause);
    }
}
