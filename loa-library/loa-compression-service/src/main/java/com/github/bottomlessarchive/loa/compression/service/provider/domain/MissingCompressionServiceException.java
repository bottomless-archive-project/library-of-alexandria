package com.github.bottomlessarchive.loa.compression.service.provider.domain;

public class MissingCompressionServiceException extends RuntimeException {

    public MissingCompressionServiceException(final String message) {
        super(message);
    }
}
