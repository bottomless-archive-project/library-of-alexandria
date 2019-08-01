package com.github.loa.indexer.service.index;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.client.service.VaultClientService;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IndexRequestFactory {

    private final VaultClientService vaultClientService;

    public IndexRequest newIndexRequest(final DocumentEntity documentEntity) {
        final InputStream inputStream = vaultClientService.queryDocument(documentEntity);

        try {
            return new IndexRequest("vault_documents")
                    .id(documentEntity.getId())
                    .source(Map.of("content", inputStream.readAllBytes()))
                    .setPipeline("vault-document-pipeline")
                    .timeout(TimeValue.timeValueMinutes(30));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
