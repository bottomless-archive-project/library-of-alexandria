package com.github.loa.stage.service.domain.exception;

public class StageAccessException extends RuntimeException {

    public StageAccessException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
