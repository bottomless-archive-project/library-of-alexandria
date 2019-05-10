package com.github.loa.vault.domain.exception;

public class VaultAccessException extends RuntimeException {

    public VaultAccessException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
