package com.github.loa.indexer.service.search.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.loa.indexer.domain.DocumentSearchEntity;
import com.github.loa.indexer.domain.IndexerAccessException;
import com.github.loa.indexer.service.search.domain.SearchDatabaseEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentSearchEntityTransformer {

    private final ObjectMapper objectMapper;

    public Flux<DocumentSearchEntity> transform(final SearchHits searchHits) {
        return Flux.fromArray(searchHits.getHits())
                .map(this::transform)
                .flatMap(Function.identity());
    }

    public Mono<DocumentSearchEntity> transform(final SearchHit searchHit) {
        try {
            final SearchDatabaseEntity searchDatabaseEntity = objectMapper
                    .readValue(searchHit.getSourceAsString(), SearchDatabaseEntity.class);

            return Mono.just(
                    DocumentSearchEntity.builder()
                            .id(searchHit.getId())
                            .title(searchDatabaseEntity.getTitle())
                            .author(searchDatabaseEntity.getAuthor())
                            .language(searchDatabaseEntity.getLanguage())
                            .description(searchDatabaseEntity.getContent())
                            .pageCount(searchDatabaseEntity.getPageCount())
                            .type(searchDatabaseEntity.getType())
                            .build()
            );
        } catch (IOException e) {
            throw new IndexerAccessException("Failed to convert source!", e);
        }
    }
}
