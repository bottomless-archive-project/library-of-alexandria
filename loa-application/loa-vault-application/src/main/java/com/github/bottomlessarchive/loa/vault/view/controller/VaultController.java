package com.github.bottomlessarchive.loa.vault.view.controller;

import com.github.bottomlessarchive.loa.document.service.DocumentManipulator;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.vault.configuration.VaultConfigurationProperties;
import com.github.bottomlessarchive.loa.vault.service.RecompressorService;
import com.github.bottomlessarchive.loa.vault.service.VaultDocumentManager;
import com.github.bottomlessarchive.loa.vault.service.backend.service.VaultDocumentStorage;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocation;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocationFactory;
import com.github.bottomlessarchive.loa.vault.view.request.domain.RecompressDocumentRequest;
import com.github.bottomlessarchive.loa.vault.view.request.domain.ReplaceDocumentRequest;
import com.github.bottomlessarchive.loa.vault.view.response.domain.DocumentExistsResponse;
import com.github.bottomlessarchive.loa.vault.view.response.domain.FreeSpaceResponse;
import com.github.bottomlessarchive.loa.vault.view.domain.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
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
     * @param documentId document's id that is being requested
     * @return the returned document's content
     */
    @GetMapping("/document/{documentId}")
    public ResponseEntity<Resource> queryDocument(@PathVariable final String documentId) {
        return documentEntityFactory.getDocumentEntity(UUID.fromString(documentId))
                .map(documentEntity -> {
                    if (!documentEntity.isInVault(vaultConfigurationProperties.name())) {
                        throw new InvalidRequestException("Document with id " + documentId + " is available on a different vault!");
                    }

                    return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .header("Content-Disposition", "attachment; filename=" + documentId + "."
                                    + documentEntity.getType().getFileExtension())
                            .cacheControl(CacheControl.noCache())
                            .body(vaultDocumentManager.readDocument(documentEntity));
                })
                .orElseThrow(() -> new InvalidRequestException("Document not found with id " + documentId + " or already removed!"));
    }

    /**
     * Removed a document from the vault and the database as well.This endpoint only accepts requests when modification
     * is enabled for this vault.
     *
     * @param documentId the document's id that should be deleted
     */
    @DeleteMapping("/document/{documentId}")
    public void deleteDocument(@PathVariable final String documentId) {
        if (!vaultConfigurationProperties.modificationEnabled()) {
            throw new InvalidRequestException("Modification is disabled on this vault instance!");
        }

        documentEntityFactory.getDocumentEntity(UUID.fromString(documentId))
                .ifPresentOrElse(documentEntity -> {
                    if (!documentEntity.isInVault(vaultConfigurationProperties.name())) {
                        throw new InvalidRequestException("Document with id " + documentId + " is available on a different vault!");
                    }

                    vaultDocumentManager.removeDocument(documentEntity);
                    documentEntityFactory.removeDocumentEntity(documentEntity);
                }, () -> {
                    throw new InvalidRequestException("Document not found with id " + documentId + " or already removed!");
                });
    }

    /**
     * Recompress a document in the vault. This endpoint only accepts requests when modification is enabled for this
     * vault.
     *
     * @param recompressRequest the request that specify which document to recompress and to what compression
     */
    @PutMapping("/document/{documentId}/recompress")
    public void recompressDocument(@PathVariable final String documentId,
            @RequestBody final RecompressDocumentRequest recompressRequest) {
        if (!vaultConfigurationProperties.modificationEnabled()) {
            throw new InvalidRequestException("Modification is disabled on this vault instance!");
        }

        documentEntityFactory.getDocumentEntity(UUID.fromString(documentId))
                .ifPresentOrElse(documentEntity -> {
                    if (!documentEntity.isInVault(vaultConfigurationProperties.name())) {
                        throw new InvalidRequestException("Document with id " + documentId + " is available on a different vault!");
                    }

                    recompressorService.recompress(documentEntity, recompressRequest.getCompression());
                }, () -> {
                    throw new InvalidRequestException("Document not found with id " + documentId + "!");
                });
    }

    /**
     * Return the free space available on this vault instance.
     *
     * @return the free space available on this instance
     */
    @GetMapping("/vault/free-space")
    public FreeSpaceResponse getFreeSpace() {
        return FreeSpaceResponse.builder()
                .freeSpace(vaultDocumentManager.getAvailableSpace())
                .build();
    }

    @GetMapping("/document/{documentId}/exists")
    public DocumentExistsResponse getDocumentExists(@PathVariable final String documentId) {
        return documentEntityFactory.getDocumentEntity(UUID.fromString(documentId))
                .map(documentEntity -> {
                    if (!documentEntity.isInVault(vaultConfigurationProperties.name())) {
                        throw new InvalidRequestException("Document with id " + documentId + " is available on a different vault!");
                    }

                    return DocumentExistsResponse.builder()
                            .exists(vaultDocumentManager.documentExists(documentEntity))
                            .build();
                })
                .orElseThrow(() -> new InvalidRequestException("Document not found with id " + documentId + "!"));
    }

    @PutMapping("/document/{documentId}/replace")
    public void replaceCorruptDocument(@PathVariable final String documentId,
            @RequestBody final ReplaceDocumentRequest replaceDocumentRequest) {
        if (!vaultConfigurationProperties.modificationEnabled()) {
            throw new InvalidRequestException("Modification is disabled on this vault instance!");
        }

        log.info("Replacing corrupt document with id: {}.", documentId);

        documentEntityFactory.getDocumentEntity(UUID.fromString(documentId))
                .ifPresentOrElse(documentEntity -> {
                    if (!documentEntity.isInVault(vaultConfigurationProperties.name())) {
                        throw new InvalidRequestException("Document with id " + documentId + " is available on a different vault!");
                    }

                    vaultDocumentManager.removeDocument(documentEntity);

                    final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity,
                            documentEntity.getCompression());

                    vaultDocumentStorage.persistDocument(documentEntity, replaceDocumentRequest.getContent(),
                            vaultLocation);

                    documentManipulator.markDownloadedSync(documentEntity.getId());
                }, () -> {
                    throw new InvalidRequestException("Document not found with id " + documentId + "!");
                });
    }
}
