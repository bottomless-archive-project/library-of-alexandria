package com.github.loa.downloader.download.service.file.domain;

public class FailedToArchiveException extends RuntimeException {

    public FailedToArchiveException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
