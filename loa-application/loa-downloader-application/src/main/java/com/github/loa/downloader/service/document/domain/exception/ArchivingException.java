package com.github.loa.downloader.service.document.domain.exception;

public class ArchivingException extends RuntimeException {

    public ArchivingException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
