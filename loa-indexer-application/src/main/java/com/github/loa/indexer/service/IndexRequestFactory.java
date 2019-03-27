package com.github.loa.indexer.service;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.indexer.service.base64.DocumentBase64Factory;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.index.IndexRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IndexRequestFactory {

    private final DocumentBase64Factory documentBase64Factory;

    public IndexRequest newIndexRequest(final DocumentEntity documentEntity) {
        final String documentBase64String = documentBase64Factory.getBase64String(documentEntity);

        final Map<String, Object> source = new HashMap<>();
        source.put("data", documentBase64String);

        return new IndexRequest("vault_search", "document", documentEntity.getId())
                .source(source)
                .setPipeline("attachment");
    }
}
