package com.github.loa.indexer.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Contains the result of a document search in the indexer repository.
 */
@Getter
@Builder
public class DocumentSearchResult {

    private final long totalHitCount;
    private final List<DocumentSearchEntity> searchHits;
}
