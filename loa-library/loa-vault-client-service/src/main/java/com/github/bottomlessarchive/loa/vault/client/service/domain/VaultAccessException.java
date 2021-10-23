package com.github.bottomlessarchive.loa.vault.client.service.domain;

public class VaultAccessException extends RuntimeException {

    public VaultAccessException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
