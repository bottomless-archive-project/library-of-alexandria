package com.github.bottomlessarchive.loa.vault.domain.exception;

/**
 * This exception is thrown when the underlying storage is inaccessible.
 */
public class StorageAccessException extends RuntimeException {

    public StorageAccessException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
