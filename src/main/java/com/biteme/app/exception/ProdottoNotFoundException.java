package com.biteme.app.exception;

public class ProdottoNotFoundException extends RuntimeException {
    public ProdottoNotFoundException(String message) {
        super(message);
    }
}