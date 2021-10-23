package com.github.loa.indexer.service.search.request.mapping.domain;

public class MappingLoadingException extends RuntimeException {

    public MappingLoadingException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
