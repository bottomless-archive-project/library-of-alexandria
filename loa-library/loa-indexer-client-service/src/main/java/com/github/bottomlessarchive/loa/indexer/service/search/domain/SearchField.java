package com.github.bottomlessarchive.loa.indexer.service.search.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This enum contains the fields that are available in the search engine.
 */
@Getter
@RequiredArgsConstructor
public enum SearchField {

    /**
     * The content of the document.
     */
    CONTENT("content"),

    /**
     * The language of the document.
     */
    LANGUAGE("language"),

    /**
     * The type of the document.
     */
    DOCUMENT_TYPE("type"),

    /**
     * The page count of the document.
     */
    PAGE_COUNT("page_count");

    /**
     * The name of the field in the search engine.
     */
    private final String name;
}
