package com.github.loa.indexer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.indexer.domain.IndexerAccessException;
import com.github.loa.indexer.domain.SearchDocumentEntity;
import com.github.loa.indexer.service.repository.SearchDatabaseEntity;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchDocumentEntityTransformer {

    private final ObjectMapper objectMapper;
    private final DocumentEntityFactory documentEntityFactory;

    public List<SearchDocumentEntity> transform(final SearchHits searchHits) {
        return Arrays.stream(searchHits.getHits())
                .map(this::transform)
                .collect(Collectors.toList());
    }

    public SearchDocumentEntity transform(final SearchHit searchHit) {
        try {
            final SearchDatabaseEntity searchDatabaseEntity = objectMapper
                    .readValue(searchHit.getSourceAsString(), SearchDatabaseEntity.class);

            return SearchDocumentEntity.builder()
                    .title(searchDatabaseEntity.getAttachment().getTitle())
                    .author(searchDatabaseEntity.getAttachment().getAuthor())
                    .language(searchDatabaseEntity.getAttachment().getLanguage())
                    .description(searchHit.getHighlightFields().get("attachment.content")
                            .getFragments()[0].string())
                    .documentEntity(documentEntityFactory.getDocumentEntity(searchHit.getId()))
                    .build();
        } catch (IOException e) {
            throw new IndexerAccessException("Failed to convert source!", e);
        }
    }
}
