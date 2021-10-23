package com.github.bottomlessarchive.loa.indexer.domain;

import lombok.Builder;
import lombok.Getter;
import reactor.core.publisher.Flux;

/**
 * Contains the result of a document search in the indexer repository.
 */
@Getter
@Builder
public class DocumentSearchResult {

    private final long totalHitCount;
    private final Flux<DocumentSearchEntity> searchHits;
}
