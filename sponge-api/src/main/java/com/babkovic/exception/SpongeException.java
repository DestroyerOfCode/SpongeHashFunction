package com.babkovic.exception;

public class SpongeException extends RuntimeException {

  public SpongeException(final String message) {
    super(message);
  }

  public SpongeException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
