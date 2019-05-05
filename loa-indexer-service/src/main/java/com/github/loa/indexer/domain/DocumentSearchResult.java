package com.github.loa.indexer.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DocumentSearchResult {

    private final long totalHitCount;
    private final List<SearchDocumentEntity> searchHits;
}
