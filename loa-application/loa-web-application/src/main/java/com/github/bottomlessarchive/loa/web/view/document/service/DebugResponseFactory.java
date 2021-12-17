package com.github.bottomlessarchive.loa.web.view.document.service;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.indexer.service.search.DocumentSearchClient;
import com.github.bottomlessarchive.loa.location.service.factory.DocumentLocationEntityFactory;
import com.github.bottomlessarchive.loa.location.service.factory.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.vault.client.service.VaultClientService;
import com.github.bottomlessarchive.loa.web.view.document.response.DebugDocumentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DebugResponseFactory {

    private final VaultClientService vaultClientService;
    private final DocumentSearchClient documentSearchClient;
    private final DocumentLocationEntityFactory documentLocationEntityFactory;

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
        return Mono.fromCallable(() -> documentSearchClient.isDocumentInIndex(documentEntity.getId()))
                .map(builder::isInIndex)
                .then();
    }

    private Mono<Void> fillEntityData(final DocumentEntity documentEntity,
            final DebugDocumentResponse.DebugDocumentResponseBuilder builder) {
        // If the source locations are empty, then the Flux.fromIterable will not run
        if (documentEntity.getSourceLocations().isEmpty()) {
            return Mono.just(Collections.<String>emptyList())
                    .doOnNext(documentSourceLocations -> populateEntityData(builder, documentEntity, documentSourceLocations))
                    .then();
        }

        return Flux.fromIterable(documentEntity.getSourceLocations())
                .flatMap(documentLocationEntityFactory::getDocumentLocation)
                .map(DocumentLocation::getUrl)
                .buffer()
                .doOnNext(documentSourceLocations -> populateEntityData(builder, documentEntity, documentSourceLocations))
                .then();
    }

    private void populateEntityData(final DebugDocumentResponse.DebugDocumentResponseBuilder builder,
            final DocumentEntity documentEntity, final List<String> documentSourceLocations) {
        builder
                .id(documentEntity.getId())
                .vault(documentEntity.getVault())
                .type(documentEntity.getType())
                .status(documentEntity.getStatus())
                .compression(documentEntity.getCompression())
                .checksum(documentEntity.getChecksum())
                .fileSize(documentEntity.getFileSize())
                .downloadDate(documentEntity.getDownloadDate())
                .downloaderVersion(documentEntity.getDownloaderVersion())
                .sourceLocations(Set.copyOf(documentSourceLocations));
    }

    private Mono<Void> fillExistsInVault(final DocumentEntity documentEntity,
            final DebugDocumentResponse.DebugDocumentResponseBuilder builder) {
        return Mono.just(documentEntity)
                .flatMap(vaultClientService::documentExists)
                .map(builder::isInVault)
                .then();
    }
}
