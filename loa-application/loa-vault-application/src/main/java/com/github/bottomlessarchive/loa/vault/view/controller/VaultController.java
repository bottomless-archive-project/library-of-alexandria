package com.github.bottomlessarchive.loa.vault.view.controller;

import com.github.bottomlessarchive.loa.document.service.DocumentManipulator;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.vault.configuration.VaultConfigurationProperties;
import com.github.bottomlessarchive.loa.vault.service.RecompressorService;
import com.github.bottomlessarchive.loa.vault.service.VaultDocumentManager;
import com.github.bottomlessarchive.loa.vault.service.backend.service.VaultDocumentStorage;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocation;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocationFactory;
import com.github.bottomlessarchive.loa.vault.view.request.domain.DeleteDocumentRequest;
import com.github.bottomlessarchive.loa.vault.view.request.domain.DocumentExistsRequest;
import com.github.bottomlessarchive.loa.vault.view.request.domain.QueryDocumentRequest;
import com.github.bottomlessarchive.loa.vault.view.request.domain.RecompressDocumentRequest;
import com.github.bottomlessarchive.loa.vault.view.request.domain.ReplaceCorruptDocumentRequest;
import com.github.bottomlessarchive.loa.vault.view.response.domain.DocumentExistsResponse;
import com.github.bottomlessarchive.loa.vault.view.response.domain.FreeSpaceResponse;
import com.github.bottomlessarchive.loa.vault.view.domain.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class VaultController {

    private final DocumentEntityFactory documentEntityFactory;
    private final VaultLocationFactory vaultLocationFactory;
    private final VaultDocumentManager vaultDocumentManager;
    private final VaultDocumentStorage vaultDocumentStorage;
    private final RecompressorService recompressorService;
    private final DocumentManipulator documentManipulator;
    private final VaultConfigurationProperties vaultConfigurationProperties;

    /**
     * Return a document's content from the vault, based on the provided document id.
     *
     * @param queryDocumentRequest the request body that contains the document's id that is being requested
     * @return the returned document's content
     */
    @MessageMapping("queryDocument")
    public Flux<DataBuffer> queryDocument(final QueryDocumentRequest queryDocumentRequest) {
        final String documentId = queryDocumentRequest.getDocumentId();

        return documentEntityFactory.getDocumentEntity(UUID.fromString(documentId))
                .switchIfEmpty(Mono.error(new InvalidRequestException("Document not found with id " + documentId + " or already removed!")))
                .flatMapMany(documentEntity -> {
                    if (!documentEntity.isInVault(vaultConfigurationProperties.getName())) {
                        return Flux.error(new InvalidRequestException(
                                "Document with id " + documentId + " is available on a different vault!"));
                    }

                    final Resource resource = vaultDocumentManager.readDocument(documentEntity);

                    return DataBufferUtils.read(resource, DefaultDataBufferFactory.sharedInstance, 8192)
                            .onErrorResume(e -> Mono.error(new InvalidRequestException("Unable to read the file for document with id "
                                    + documentId + "!", e)));
                });
    }

    /**
     * Removed a document from the vault and the database as well.This endpoint only accepts requests when modification
     * is enabled for this vault.
     *
     * @param deleteDocumentRequest the request body that contains the document's id that should be deleted
     * @return an empty response
     */
    @MessageMapping("deleteDocument")
    public Mono<Void> deleteDocument(final DeleteDocumentRequest deleteDocumentRequest) {
        if (!vaultConfigurationProperties.isModificationEnabled()) {
            return Mono.error(new InvalidRequestException("Modification is disabled on this vault instance!"));
        }

        final String documentId = deleteDocumentRequest.getDocumentId();

        return documentEntityFactory.getDocumentEntity(UUID.fromString(documentId))
                .switchIfEmpty(Mono.error(new InvalidRequestException("Document not found with id " + documentId + " or already removed!")))
                .flatMap(documentEntity -> {
                    if (!documentEntity.isInVault(vaultConfigurationProperties.getName())) {
                        return Mono.error(new InvalidRequestException("Document with id " + documentId
                                + " is available on a different vault!"));
                    }

                    return vaultDocumentManager.removeDocument(documentEntity)
                            .thenReturn(documentEntity);
                })
                .flatMap(documentEntityFactory::removeDocumentEntity)
                .then();
    }

    /**
     * Recompress a document in the vault. This endpoint only accepts requests when modification is enabled for this
     * vault.
     *
     * @param recompressRequest the request that specify which document to recompress and to what compression
     * @return an empty response
     */
    @MessageMapping("recompressDocument")
    public Mono<Void> recompressDocument(final RecompressDocumentRequest recompressRequest) {
        if (!vaultConfigurationProperties.isModificationEnabled()) {
            return Mono.error(new InvalidRequestException("Modification is disabled on this vault instance!"));
        }

        final String documentId = recompressRequest.getDocumentId();

        return documentEntityFactory.getDocumentEntity(UUID.fromString(documentId))
                .doOnNext(documentEntity -> {
                    if (!documentEntity.isInVault(vaultConfigurationProperties.getName())) {
                        throw new InvalidRequestException("Document with id " + documentId + " is available on a different vault!");
                    }

                    recompressorService.recompress(documentEntity, recompressRequest.getCompression());
                })
                .switchIfEmpty(Mono.error(new InvalidRequestException("Document not found with id " + documentId + "!")))
                .then();
    }

    /**
     * Return the free space available on this vault instance.
     *
     * @return the free space available on this instance
     */
    @MessageMapping("freeSpace")
    public Mono<FreeSpaceResponse> getFreeSpace() {
        return vaultDocumentManager.getAvailableSpace()
                .map(freeSpace -> FreeSpaceResponse.builder()
                        .freeSpace(freeSpace)
                        .build()
                );
    }

    @MessageMapping("documentExists")
    public Mono<DocumentExistsResponse> getDocumentExists(final DocumentExistsRequest documentExistsRequest) {
        final String documentId = documentExistsRequest.getDocumentId();

        return documentEntityFactory.getDocumentEntity(UUID.fromString(documentId))
                .flatMap(documentEntity -> {
                    if (!documentEntity.isInVault(vaultConfigurationProperties.getName())) {
                        return Mono.error(new InvalidRequestException("Document with id " + documentId
                                + " is available on a different vault!"));
                    }

                    return vaultDocumentManager.documentExists(documentEntity);
                })
                .map(exists -> DocumentExistsResponse.builder()
                        .exists(exists)
                        .build()
                );
    }

    @MessageMapping("replaceCorruptDocument")
    public Mono<Void> replaceCorruptDocument(final ReplaceCorruptDocumentRequest replaceCorruptDocumentRequest) {
        if (!vaultConfigurationProperties.isModificationEnabled()) {
            return Mono.error(new InvalidRequestException("Modification is disabled on this vault instance!"));
        }

        final String documentId = replaceCorruptDocumentRequest.getDocumentId();

        log.info("Replacing corrupt document with id: {}.", documentId);

        return documentEntityFactory.getDocumentEntity(UUID.fromString(documentId))
                .flatMap(documentEntity -> {
                    if (!documentEntity.isInVault(vaultConfigurationProperties.getName())) {
                        throw new InvalidRequestException("Document with id " + documentId + " is available on a different vault!");
                    }

                    return vaultDocumentManager.removeDocument(documentEntity)
                            .doOnNext(processedEntity -> {
                                final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity,
                                        documentEntity.getCompression());

                                vaultDocumentStorage.persistDocument(processedEntity, replaceCorruptDocumentRequest.getContent(),
                                        vaultLocation);
                            })
                            .flatMap(finalEntity -> documentManipulator.markDownloaded(finalEntity.getId()))
                            .then(Mono.just(documentEntity));
                })
                .switchIfEmpty(Mono.error(new InvalidRequestException("Document not found with id " + documentId + "!")))
                .then();
    }
}
