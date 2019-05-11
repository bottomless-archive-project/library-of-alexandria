package com.github.loa.indexer.service.index;

import com.github.loa.document.service.domain.DocumentEntity;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.index.IndexRequest;
import org.springframework.stereotype.Service;
import com.github.loa.indexer.service.index.base64.DocumentBase64Encoder;

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
                .setPipeline("vault-document-pipeline");
    }
}
