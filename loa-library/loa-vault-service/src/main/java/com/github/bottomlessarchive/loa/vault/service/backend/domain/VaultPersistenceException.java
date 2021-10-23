package com.github.bottomlessarchive.loa.vault.service.backend.domain;

/**
 * Thrown when the application fails to persist a document to the vault. The reason could vary but
 * predominantly IO related.
 */
public class VaultPersistenceException extends RuntimeException {

    public VaultPersistenceException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
