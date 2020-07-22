package com.github.loa.stage.service.domain.exception;

/**
 * Thrown when an error happens while accessing content on the staging area.
 */
public class StageAccessException extends RuntimeException {

    public StageAccessException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
