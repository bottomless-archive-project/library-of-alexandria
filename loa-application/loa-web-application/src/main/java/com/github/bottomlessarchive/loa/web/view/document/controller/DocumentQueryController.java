package com.github.bottomlessarchive.loa.web.view.document.controller;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.document.view.service.MediaTypeCalculator;
import com.github.bottomlessarchive.loa.vault.client.service.VaultClientService;
import com.github.bottomlessarchive.loa.web.view.document.service.DocumentRenderer;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * A controller that provides access to the documents available in the vault.
 */
@RestController
@RequiredArgsConstructor
public class DocumentQueryController {

    private final DocumentRenderer documentRenderer;
    private final DocumentEntityFactory documentEntityFactory;
    private final VaultClientService vaultClientService;
    private final MediaTypeCalculator mediaTypeCalculator;

    /**
     * Return a document's content from the vault, based on the provided document id.
     *
     * @param documentId the id of the document to return the content for
     * @return the returned document's content
     */
    @GetMapping("/document/{documentId}")
    public ResponseEntity<InputStreamResource> queryDocument(@PathVariable final String documentId) {
        return documentEntityFactory.getDocumentEntity(UUID.fromString(documentId))
                .map(documentEntity -> {
                    final InputStream documentContent = vaultClientService.queryDocument(documentEntity);

                    return ResponseEntity.ok()
                            .contentType(mediaTypeCalculator.calculateMediaType(documentEntity.getType()))
                            .cacheControl(CacheControl.noCache())
                            .body(new InputStreamResource(documentContent));
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found with id " + documentId + "!"));
    }

    @GetMapping(path = "/document/{documentId}/image", produces = "image/png")
    public byte[] queryDocumentImage(@PathVariable final String documentId) {
        return documentEntityFactory.getDocumentEntity(UUID.fromString(documentId))
                .map(documentEntity -> {
                    if (documentEntity.getType() != DocumentType.PDF) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Images can only be rendered for documents that are in a PDF format.");
                    }

                    try (InputStream documentContent = vaultClientService.queryDocument(documentEntity)) {
                        return documentRenderer.renderFirstPage(documentContent);
                    } catch (IOException e) {
                        throw new IllegalStateException("Failed to query document from vault!", e);
                    }
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found with id " + documentId + "!"));
    }
}
