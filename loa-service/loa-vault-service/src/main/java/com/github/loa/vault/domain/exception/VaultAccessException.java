package com.github.loa.vault.domain.exception;

/**
 * This exception is thrown when the vault is inaccessible.
 */
public class VaultAccessException extends RuntimeException {

    public VaultAccessException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
