package com.github.bottomlessarchive.loa.source.file.service.domain;

public class FileHandlingException extends RuntimeException {

    public FileHandlingException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
