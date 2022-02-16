package com.github.bottomlessarchive.loa.indexer.service.indexer;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.github.bottomlessarchive.loa.document.service.DocumentManipulator;
import com.github.bottomlessarchive.loa.indexer.service.indexer.domain.IndexingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexerClient {

    private final IndexRequestFactory indexRequestFactory;
    private final ElasticsearchClient elasticsearchClient;
    private final DocumentManipulator documentManipulator;

    public void indexDocument(final IndexingContext indexingContext) {
        final UUID documentId = indexingContext.getId();

        if (indexingContext.getContent() == null) {
            log.info("Marking {} as indexed, even if it has no parsable content!", documentId);

            documentManipulator.markIndexed(documentId);

            return;
        }

        try {
            log.info("Indexing document with id: {}.", indexingContext.getId());

            sendIndexRequest(indexingContext);

            documentManipulator.markIndexed(documentId);
        } catch (final IOException | ElasticsearchException e) {
            log.warn("Failed to index document {}! Cause: '{}'.", documentId, e.getMessage());

            documentManipulator.markCorrupt(documentId);
        }
    }

    private void sendIndexRequest(final IndexingContext documentMetadata) throws IOException {
        final IndexRequest<Map<String, Object>> indexRequest = indexRequestFactory.newIndexRequest(documentMetadata);

        elasticsearchClient.index(indexRequest);
    }
}
