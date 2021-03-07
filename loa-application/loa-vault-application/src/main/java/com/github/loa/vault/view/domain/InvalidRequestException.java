package com.github.loa.vault.view.domain;

public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(final String message) {
        super(message);
    }

    public InvalidRequestException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
