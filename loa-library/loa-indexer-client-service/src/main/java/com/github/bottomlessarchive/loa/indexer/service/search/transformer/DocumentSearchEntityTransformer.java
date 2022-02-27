package com.github.bottomlessarchive.loa.indexer.service.search.transformer;

import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.DocumentSearchEntity;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.SearchDatabaseEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentSearchEntityTransformer {

    public List<DocumentSearchEntity> transform(final HitsMetadata<SearchDatabaseEntity> searchHits) {
        return searchHits.hits().stream()
                .map(this::transform)
                .toList();
    }

    private DocumentSearchEntity transform(final Hit<SearchDatabaseEntity> searchHit) {
        final SearchDatabaseEntity searchDatabaseEntity = searchHit.source();

        if (searchDatabaseEntity == null) {
            throw new IllegalStateException("Document source is null in the index for document with id: " + searchHit.id() + "!");
        }

        return DocumentSearchEntity.builder()
                .id(searchHit.id())
                .title(searchDatabaseEntity.getTitle())
                .author(searchDatabaseEntity.getAuthor())
                .language(searchDatabaseEntity.getLanguage())
                .description(
                        searchHit.highlight().get("content").stream()
                                .map(fragment -> fragment
                                        .replaceAll("(\\r\\n|\\n)", "")
                                        .replace("•", "")
                                        .trim()
                                )
                                .toList()
                )
                .pageCount(searchDatabaseEntity.getPageCount())
                .type(searchDatabaseEntity.getType())
                .build();
    }
}
