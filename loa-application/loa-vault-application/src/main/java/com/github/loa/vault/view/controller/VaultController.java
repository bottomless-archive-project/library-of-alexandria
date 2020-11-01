package com.github.loa.vault.view.controller;

import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.document.view.service.MediaTypeCalculator;
import com.github.loa.vault.configuration.VaultConfigurationProperties;
import com.github.loa.vault.service.RecompressorService;
import com.github.loa.vault.service.VaultDocumentManager;
import com.github.loa.vault.view.request.domain.RecompressRequest;
import com.github.loa.vault.view.response.domain.FreeSpaceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class VaultController {

    private final DocumentEntityFactory documentEntityFactory;
    private final VaultDocumentManager vaultDocumentManager;
    private final RecompressorService recompressorService;
    private final MediaTypeCalculator mediaTypeCalculator;
    private final VaultConfigurationProperties vaultConfigurationProperties;

    /**
     * Return a document's content from the vault, based on the provided document id.
     *
     * @param documentId the id of the document to return the content for
     * @return the returned document's content
     */
    @GetMapping("/document/{documentId}")
    public Mono<ResponseEntity<Resource>> queryDocument(@PathVariable final String documentId) {
        return documentEntityFactory.getDocumentEntity(UUID.fromString(documentId))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document not found with id " + documentId + " or already removed!")))
                .map(documentEntity -> {
                    if (!documentEntity.isInVault(vaultConfigurationProperties.getName())) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Document with id " + documentId
                                + " is available on a different vault!");
                    }

                    final Resource resource = vaultDocumentManager.readDocument(documentEntity);

                    return ResponseEntity.ok()
                            .contentType(mediaTypeCalculator.calculateMediaType(documentEntity.getType()))
                            .cacheControl(CacheControl.noCache())
                            .body(resource);
                });
    }

    /**
     * Removed a document from the vault and the database as well.This endpoint only accepts requests when modification
     * is enabled for this vault.
     *
     * @param documentId the id of the document to remove
     * @return an empty response
     */
    @DeleteMapping("/document/{documentId}")
    public Mono<Void> removeDocument(@PathVariable final String documentId) {
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
     * @param documentId        the id of the document to recompress
     * @param recompressRequest the request for recompression
     * @return an empty response
     */
    @PostMapping("/document/{documentId}/recompress")
    public Mono<Void> recompressDocument(@PathVariable final String documentId,
            @RequestBody final RecompressRequest recompressRequest) {
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

    @GetMapping("/free-space")
    public Mono<FreeSpaceResponse> getFreeSpace() {
        return vaultDocumentManager.getAvailableSpace()
                .map(freeSpace -> FreeSpaceResponse.builder()
                        .freeSpace(freeSpace)
                        .build()
                );
    }
}
