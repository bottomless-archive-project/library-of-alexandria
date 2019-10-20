package com.github.loa.indexer.service.search.domain;

import com.github.loa.document.service.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Getter
@Builder
public class SearchContext {

    private final String keyword;
    private final String language;
    private final boolean exactMatch;
    private final DocumentLength documentLength;
    private final List<DocumentType> documentTypes;

    @Builder.Default
    private final int pageNumber = 0;

    public Optional<String> getLanguage() {
        return Optional.ofNullable(language);
    }

    public Optional<DocumentLength> getDocumentLength() {
        return Optional.ofNullable(documentLength);
    }

    public List<DocumentType> getDocumentTypes() {
        return documentTypes != null ? documentTypes : Collections.emptyList();
    }
}
