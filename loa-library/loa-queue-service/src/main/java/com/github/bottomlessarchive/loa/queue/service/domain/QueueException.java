package com.github.bottomlessarchive.loa.queue.service.domain;

public class QueueException extends RuntimeException {

    public QueueException(final String message) {
        super(message);
    }

    public QueueException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
