package com.github.loa.indexer.domain;

public class IndexerAccessException extends RuntimeException {

    public IndexerAccessException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
