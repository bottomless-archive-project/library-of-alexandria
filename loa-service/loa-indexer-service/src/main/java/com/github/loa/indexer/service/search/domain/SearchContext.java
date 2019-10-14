package com.github.loa.indexer.service.search.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

@Getter
@Builder
public class SearchContext {

    private final String keyword;
    private final String language;
    private final boolean exactMatch;
    private final DocumentLength documentLength;

    @Builder.Default
    private final int pageNumber = 0;

    public Optional<String> getLanguage() {
        return Optional.ofNullable(language);
    }

    public Optional<DocumentLength> getDocumentLength() {
        return Optional.ofNullable(documentLength);
    }
}
