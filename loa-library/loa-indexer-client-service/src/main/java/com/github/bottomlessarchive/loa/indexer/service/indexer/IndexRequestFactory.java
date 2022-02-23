package com.github.bottomlessarchive.loa.indexer.service.indexer;

import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.github.bottomlessarchive.loa.indexer.service.indexer.domain.IndexingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexRequestFactory {

    private final IndexDocumentFactory indexDocumentFactory;

    public IndexRequest<Map<String, Object>> newIndexRequest(final IndexingContext indexingContext) {
        return new IndexRequest.Builder<Map<String, Object>>()
                .id(indexingContext.getId().toString())
                .index("vault_documents")
                .document(indexDocumentFactory.buildIndexDocument(indexingContext))
                .timeout(
                        new Time.Builder()
                                .time("30s")
                                .build()
                )
                .build();
    }
}
