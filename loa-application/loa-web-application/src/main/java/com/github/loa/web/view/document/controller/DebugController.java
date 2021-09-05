package com.github.loa.web.view.document.controller;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.indexer.service.search.DocumentSearchService;
import com.github.loa.vault.client.service.VaultClientService;
import com.github.loa.web.view.document.response.DebugDocumentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class DebugController {

    private final VaultClientService vaultClientService;
    private final DocumentSearchService documentSearchService;
    private final DocumentEntityFactory documentEntityFactory;

    @GetMapping("/document/{documentId}/debug")
    public Mono<DebugDocumentResponse> getDocumentById(@PathVariable final String documentId) {
        final DebugDocumentResponse.DebugDocumentResponseBuilder builder =
                DebugDocumentResponse.builder();

        return documentEntityFactory.getDocumentEntity(UUID.fromString(documentId))
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found with id: " + documentId + "!")
                ))
                .flatMap(documentEntity -> Mono.when(
                                        this.fillIndexData(documentEntity, builder),
                                        this.fillEntityData(documentEntity, builder),
                                        this.fillExistsInVault(documentEntity, builder)
                                )
                                .thenReturn(builder)
                )
                .then(Mono.defer(() -> Mono.just(builder.build())));
    }

    private Mono<Void> fillIndexData(final DocumentEntity documentEntity, final DebugDocumentResponse.DebugDocumentResponseBuilder builder) {
        return Mono.fromCallable(() -> documentSearchService.isDocumentInIndex(documentEntity.getId()))
                .map(builder::isInIndex)
                .then();
    }

    private Mono<Void> fillEntityData(final DocumentEntity documentEntity, final DebugDocumentResponse.DebugDocumentResponseBuilder builder) {
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

    private Mono<Void> fillExistsInVault(final DocumentEntity documentEntity, final DebugDocumentResponse.DebugDocumentResponseBuilder builder) {
        return Mono.just(documentEntity)
                .flatMap(vaultClientService::documentExists)
                .map(builder::isInVault)
                .then();
    }
}
