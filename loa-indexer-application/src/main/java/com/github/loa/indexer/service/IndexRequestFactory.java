package com.github.loa.indexer.service;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.indexer.service.base64.DocumentBase64Encoder;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.index.IndexRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IndexRequestFactory {

    private final DocumentBase64Encoder documentBase64Encoder;

    public IndexRequest newIndexRequest(final DocumentEntity documentEntity) {
        final String documentBase64String = documentBase64Encoder.encodeDocument(documentEntity);

        return new IndexRequest("vault_documents")
                .id(documentEntity.getId())
                .source(Map.of("content", documentBase64String))
                .setPipeline("attachment");
    }
}
