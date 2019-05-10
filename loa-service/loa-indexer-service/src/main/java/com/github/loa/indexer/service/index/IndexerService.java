package com.github.loa.indexer.service.index;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.indexer.service.index.response.IndexResponseActionListenerFactory;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndexerService {

    private final IndexRequestFactory indexRequestFactory;
    private final RestHighLevelClient restHighLevelClient;
    private final IndexResponseActionListenerFactory indexResponseActionListenerFactory;

    public void indexDocument(final DocumentEntity documentEntity) {
        final IndexRequest indexRequest = indexRequestFactory.newIndexRequest(documentEntity);

        restHighLevelClient.indexAsync(indexRequest, RequestOptions.DEFAULT,
                indexResponseActionListenerFactory.newListener());
    }
}
