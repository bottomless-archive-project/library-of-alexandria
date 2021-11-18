package com.github.bottomlessarchive.loa.indexer.service.search.domain;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DocumentSearchEntity {

    private final String id;
    private final String title;
    private final String author;
    private final List<String> description;
    private final String language;
    private final int pageCount;
    private final DocumentType type;
}
