package com.github.loa.indexer.service.index;

import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.parser.domain.DocumentMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexerService {

    private final IndexRequestFactory indexRequestFactory;
    private final RestHighLevelClient restHighLevelClient;
    private final DocumentManipulator documentManipulator;

    public void indexDocuments(final DocumentMetadata documentMetadata) {
        final String documentId = documentMetadata.getId();

        indexRequestFactory.newIndexRequest(documentMetadata)
                .subscribe(indexRequest -> {
                    try {
                        restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

                        documentManipulator.markIndexed(documentId).subscribe();
                    } catch (IOException | ElasticsearchException e) {
                        log.info("Failed to index document " + documentId + "! Cause: '" + e.getMessage() + "'.");

                        documentManipulator.markIndexFailure(documentId).subscribe();
                    }
                });
    }
}
