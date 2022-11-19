package com.github.bottomlessarchive.loa.indexer.service.indexer.domain;

import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class IndexingContext {

    private final UUID id;
    private final String content;
    private final String title;
    private final String author;
    private final String date;
    private final String language;
    private final DocumentType type;
    private final int pageCount;
}
