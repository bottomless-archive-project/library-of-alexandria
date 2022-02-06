package com.github.bottomlessarchive.loa.indexer.service.indexer;

import com.github.bottomlessarchive.loa.document.service.DocumentManipulator;
import com.github.bottomlessarchive.loa.indexer.service.indexer.domain.IndexingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexerClient {

    private final IndexRequestFactory indexRequestFactory;
    private final RestHighLevelClient restHighLevelClient;
    private final DocumentManipulator documentManipulator;

    public void indexDocument(final IndexingContext documentMetadata) {
        final UUID documentId = documentMetadata.getId();

        indexRequestFactory.newIndexRequest(documentMetadata)
                .subscribe(indexRequest -> {
                    try {
                        restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

                        documentManipulator.markIndexed(documentId).subscribe();
                    } catch (final IOException | ElasticsearchException e) {
                        if (log.isInfoEnabled()) {
                            log.info("Failed to index document {}! Cause: '{}'.", documentId, e.getMessage());
                        }

                        documentManipulator.markCorrupt(documentId);
                    }
                });
    }
}
