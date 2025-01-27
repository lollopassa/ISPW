package com.biteme.app.exception;

public class SceneLoadingException extends RuntimeException {
  public SceneLoadingException(String message, Throwable cause) {
    super(message, cause);
  }

  public SceneLoadingException(String message) {
    super(message);
  }
}