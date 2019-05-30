package com.github.loa.indexer.service.index;

import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.indexer.service.index.base64.domain.Base64EncodingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexerService {

    private final IndexRequestFactory indexRequestFactory;
    private final RestHighLevelClient restHighLevelClient;
    private final DocumentManipulator documentManipulator;
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);
    private final Semaphore semaphore = new Semaphore(25);

    public void indexDocuments(final List<DocumentEntity> documentEntities) {
        for (DocumentEntity documentEntity : documentEntities) {
            final IndexRequest indexRequest = indexRequestFactory.newIndexRequest(documentEntity);

            try {
                semaphore.acquire();

                executorService.submit(() -> {
                    try {
                        restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

                        documentManipulator.markIndexed(documentEntity.getId());
                    } catch (IOException | ElasticsearchException | Base64EncodingException e) {
                        log.info("Failed to index document " + documentEntity.getId() + "! Cause: '"
                                + e.getMessage() + "'.");

                        documentManipulator.markIndexFailure(documentEntity.getId());
                    }

                    semaphore.release();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
