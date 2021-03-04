package com.github.loa.vault.view.controller;

import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.vault.configuration.VaultConfigurationProperties;
import com.github.loa.vault.service.RecompressorService;
import com.github.loa.vault.service.VaultDocumentManager;
import com.github.loa.vault.view.request.domain.DeleteDocumentRequest;
import com.github.loa.vault.view.request.domain.QueryDocumentRequest;
import com.github.loa.vault.view.request.domain.RecompressRequest;
import com.github.loa.vault.view.response.domain.FreeSpaceResponse;
import com.github.loa.vault.view.response.domain.QueryDocumentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class VaultController {

    private final DocumentEntityFactory documentEntityFactory;
    private final VaultDocumentManager vaultDocumentManager;
    private final RecompressorService recompressorService;
    private final VaultConfigurationProperties vaultConfigurationProperties;

    /**
     * Return a document's content from the vault, based on the provided document id.
     *
     * @param queryDocumentRequest the request body that contains the document's id that is being requested
     * @return the returned document's content
     */
    @MessageMapping("queryDocument")
    public Mono<QueryDocumentResponse> queryDocument(final QueryDocumentRequest queryDocumentRequest) {
        final String documentId = queryDocumentRequest.getDocumentId();

        return documentEntityFactory.getDocumentEntity(UUID.fromString(documentId))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document not found with id " + documentId + " or already removed!")))
                .map(documentEntity -> {
                    if (!documentEntity.isInVault(vaultConfigurationProperties.getName())) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Document with id " + documentId
                                + " is available on a different vault!");
                    }

                    final Resource resource = vaultDocumentManager.readDocument(documentEntity);

                    try {
                        return QueryDocumentResponse.builder()
                                .payload(resource.getInputStream().readAllBytes())
                                .build();
                    } catch (IOException e) {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to read the file for document with id "
                                + documentId + "!");
                    }
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
    public Mono<Void> removeDocument(final DeleteDocumentRequest deleteDocumentRequest) {
        final String documentId = deleteDocumentRequest.getDocumentId();

        if (!vaultConfigurationProperties.isModificationEnabled()) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Modification is disabled on this vault instance!"));
        }

        return documentEntityFactory.getDocumentEntity(UUID.fromString(documentId))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document not found with id " + documentId + " or already removed!")))
                .flatMap(documentEntity -> {
                    if (!documentEntity.isInVault(vaultConfigurationProperties.getName())) {
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Document with id "
                                + documentId + " is available on a different vault!"));
                    }

                    return vaultDocumentManager.removeDocument(documentEntity)
                            .thenReturn(documentEntity);
                })
                .flatMap(documentEntity -> documentEntityFactory.removeDocumentEntity(documentEntity.getId()))
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
    public Mono<Void> recompressDocument(final RecompressRequest recompressRequest) {
        final String documentId = recompressRequest.getDocumentId();

        if (!vaultConfigurationProperties.isModificationEnabled()) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Modification is disabled on this vault instance!"));
        }

        return documentEntityFactory.getDocumentEntity(UUID.fromString(documentId))
                .doOnNext(documentEntity -> {
                    if (!documentEntity.isInVault(vaultConfigurationProperties.getName())) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Document with id " + documentId
                                + " is available on a different vault!");
                    }

                    recompressorService.recompress(documentEntity, recompressRequest.getCompression());
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document not found with id " + documentId + "!")))
                .then();
    }

    @MessageMapping("freeSpace")
    public Mono<FreeSpaceResponse> getFreeSpace() {
        return vaultDocumentManager.getAvailableSpace()
                .map(freeSpace -> FreeSpaceResponse.builder()
                        .freeSpace(freeSpace)
                        .build()
                );
    }
}
