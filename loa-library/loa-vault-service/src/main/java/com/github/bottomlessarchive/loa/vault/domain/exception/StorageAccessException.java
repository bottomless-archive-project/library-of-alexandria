package com.github.bottomlessarchive.loa.vault.domain.exception;

import lombok.experimental.StandardException;

/**
 * This exception is thrown when the underlying storage is inaccessible.
 */
@StandardException
public class StorageAccessException extends RuntimeException {
}
