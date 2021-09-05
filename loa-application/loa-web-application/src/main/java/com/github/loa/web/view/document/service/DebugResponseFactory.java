package com.github.loa.web.view.document.service;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.indexer.service.search.DocumentSearchService;
import com.github.loa.vault.client.service.VaultClientService;
import com.github.loa.web.view.document.response.DebugDocumentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DebugResponseFactory {

    private final VaultClientService vaultClientService;
    private final DocumentSearchService documentSearchService;

    public Mono<DebugDocumentResponse> newDebugDocumentResponse(final DocumentEntity documentEntity) {
        final DebugDocumentResponse.DebugDocumentResponseBuilder builder =
                DebugDocumentResponse.builder();

        return Mono.when(
                        this.fillIndexData(documentEntity, builder),
                        this.fillEntityData(documentEntity, builder),
                        this.fillExistsInVault(documentEntity, builder)
                )
                .then(Mono.defer(() -> Mono.just(builder.build())));
    }

    private Mono<Void> fillIndexData(final DocumentEntity documentEntity,
            final DebugDocumentResponse.DebugDocumentResponseBuilder builder) {
        return Mono.fromCallable(() -> documentSearchService.isDocumentInIndex(documentEntity.getId()))
                .map(builder::isInIndex)
                .then();
    }

    private Mono<Void> fillEntityData(final DocumentEntity documentEntity,
            final DebugDocumentResponse.DebugDocumentResponseBuilder builder) {
        return Mono.just(documentEntity)
                .map(entity -> builder
                        .id(entity.getId())
                        .vault(entity.getVault())
                        .type(entity.getType())
                        .status(entity.getStatus())
                        .compression(entity.getCompression())
                        .checksum(entity.getChecksum())
                        .fileSize(entity.getFileSize())
                        .downloadDate(entity.getDownloadDate())
                        .downloaderVersion(entity.getDownloaderVersion())
                )
                .then();
    }

    private Mono<Void> fillExistsInVault(final DocumentEntity documentEntity,
            final DebugDocumentResponse.DebugDocumentResponseBuilder builder) {
        return Mono.just(documentEntity)
                .flatMap(vaultClientService::documentExists)
                .map(builder::isInVault)
                .then();
    }
}
