package com.github.bottomlessarchive.loa.vault.view.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidRequestException extends ResponseStatusException {

    public InvalidRequestException(final String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public InvalidRequestException(final String message, final Throwable throwable) {
        super(HttpStatus.BAD_REQUEST, message, throwable);
    }
}
