package com.github.loa.indexer.service.search.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.indexer.domain.IndexerAccessException;
import com.github.loa.indexer.domain.DocumentSearchEntity;
import com.github.loa.indexer.service.search.domain.SearchDatabaseEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentSearchEntityTransformer {

    private final ObjectMapper objectMapper;
    private final DocumentEntityFactory documentEntityFactory;

    public List<DocumentSearchEntity> transform(final SearchHits searchHits) {
        return Arrays.stream(searchHits.getHits())
                .map(this::transform)
                .collect(Collectors.toList());
    }

    public DocumentSearchEntity transform(final SearchHit searchHit) {
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
                                    .documentEntity(documentEntity)
                                    .build()
                    )
                    .orElseThrow(() -> new NoSuchElementException("Missing document " + searchHit.getId()
                            + " from the database!"));
        } catch (IOException e) {
            throw new IndexerAccessException("Failed to convert source!", e);
        }
    }
}
