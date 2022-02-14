package com.github.bottomlessarchive.loa.indexer.service.search.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.DocumentSearchEntity;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.IndexerAccessException;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.SearchDatabaseEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentSearchEntityTransformer {

    private final ObjectMapper objectMapper;

    public List<DocumentSearchEntity> transform(final SearchHits searchHits) {
        return Arrays.stream(searchHits.getHits())
                .map(this::transform)
                .toList();
    }

    public DocumentSearchEntity transform(final SearchHit searchHit) {
        final SearchDatabaseEntity searchDatabaseEntity = convertToEntity(searchHit);

        return DocumentSearchEntity.builder()
                .id(searchHit.getId())
                .title(searchDatabaseEntity.getTitle())
                .author(searchDatabaseEntity.getAuthor())
                .language(searchDatabaseEntity.getLanguage())
                .description(
                        Arrays.stream(searchHit.getHighlightFields().get("content").getFragments())
                                .map(fragment -> fragment.string()
                                        .replaceAll("(\\r\\n|\\n)", "")
                                        .replace("•", "")
                                )
                                .toList()
                )
                .pageCount(searchDatabaseEntity.getPageCount())
                .type(searchDatabaseEntity.getType())
                .build();
    }

    private SearchDatabaseEntity convertToEntity(final SearchHit searchHit) {
        try {
            return objectMapper.readValue(searchHit.getSourceAsString(), SearchDatabaseEntity.class);
        } catch (IOException e) {
            throw new IndexerAccessException("Failed to convert source!", e);
        }
    }
}
