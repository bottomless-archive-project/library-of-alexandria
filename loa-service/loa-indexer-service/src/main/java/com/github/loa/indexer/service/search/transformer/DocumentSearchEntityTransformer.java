package com.github.loa.indexer.service.search.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
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
    private final DocumentEntityFactory documentEntityFactory;

    public Flux<DocumentSearchEntity> transform(final SearchHits searchHits) {
        return Flux.fromArray(searchHits.getHits())
                .map(this::transform)
                .flatMap(Function.identity());
    }

    public Mono<DocumentSearchEntity> transform(final SearchHit searchHit) {
        try {
            final SearchDatabaseEntity searchDatabaseEntity = objectMapper
                    .readValue(searchHit.getSourceAsString(), SearchDatabaseEntity.class);

            return documentEntityFactory.getDocumentEntity(searchHit.getId())
                    .map(documentEntity ->
                            DocumentSearchEntity.builder()
                                    .title(searchDatabaseEntity.getAttachment().getTitle())
                                    .author(searchDatabaseEntity.getAttachment().getAuthor())
                                    .language(searchDatabaseEntity.getAttachment().getLanguage())
                                    .description(searchHit.getHighlightFields().get("attachment.content")
                                            .getFragments()[0].string())
                                    .pageCount(searchDatabaseEntity.getPageCount())
                                    .documentEntity(documentEntity)
                                    .build()
                    );
        } catch (IOException e) {
            throw new IndexerAccessException("Failed to convert source!", e);
        }
    }
}
