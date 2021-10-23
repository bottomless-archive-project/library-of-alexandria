package com.github.bottomlessarchive.loa.compression.domain;

public class CompressionException extends RuntimeException {

    public CompressionException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
