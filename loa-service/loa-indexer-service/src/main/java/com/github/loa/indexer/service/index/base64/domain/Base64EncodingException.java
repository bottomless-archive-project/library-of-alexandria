package com.github.loa.indexer.service.index.base64.domain;

public class Base64EncodingException extends RuntimeException {

    public Base64EncodingException(final String message) {
        super(message);
    }

    public Base64EncodingException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
