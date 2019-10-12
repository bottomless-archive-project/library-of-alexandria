package com.github.loa.indexer.service.index;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.client.service.VaultClientService;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class IndexRequestFactory {

    private final VaultClientService vaultClientService;

    public Mono<IndexRequest> newIndexRequest(final DocumentEntity documentEntity, final int pageCount) {
        return vaultClientService.queryDocument(documentEntity)
                .map(documentContents -> new IndexRequest("vault_documents")
                        .id(documentEntity.getId())
                        .source(Map.of("content", documentContents, "page_count", pageCount))
                        .setPipeline("vault-document-pipeline")
                        .timeout(TimeValue.timeValueMinutes(30))
                );
    }
}
