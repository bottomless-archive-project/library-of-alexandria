package com.github.loa.indexer.domain;

import com.github.loa.document.service.domain.DocumentEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentSearchEntity {

    private final String title;
    private final String author;
    private final String description;
    private final String language;
    private final DocumentEntity documentEntity;
}
