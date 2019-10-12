package com.github.loa.indexer.service.index;

import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.document.service.domain.DocumentEntity;
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

    public void indexDocuments(final DocumentEntity documentEntity, final int pageCount) {
        indexRequestFactory.newIndexRequest(documentEntity, pageCount)
                .subscribe(indexRequest -> {
                    try {
                        restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

                        documentManipulator.markIndexed(documentEntity.getId());
                    } catch (IOException | ElasticsearchException e) {
                        log.info("Failed to index document " + documentEntity.getId() + "! Cause: '" + e.getMessage() + "'.");

                        documentManipulator.markIndexFailure(documentEntity.getId());
                    }
                });
    }
}
