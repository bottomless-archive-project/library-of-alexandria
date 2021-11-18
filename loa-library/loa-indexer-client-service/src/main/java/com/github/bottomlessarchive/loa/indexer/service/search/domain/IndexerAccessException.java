package com.github.bottomlessarchive.loa.indexer.service.search.domain;

public class IndexerAccessException extends RuntimeException {

    public IndexerAccessException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
