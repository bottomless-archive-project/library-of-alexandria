package com.github.loa.queue.service.domain;

public class QueueException extends RuntimeException {

    public QueueException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
