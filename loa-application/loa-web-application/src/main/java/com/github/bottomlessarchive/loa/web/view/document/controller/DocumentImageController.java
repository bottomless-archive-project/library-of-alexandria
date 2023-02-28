package com.github.bottomlessarchive.loa.web.view.document.controller;

import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.bottomlessarchive.loa.vault.client.service.VaultClientService;
import com.github.bottomlessarchive.loa.web.view.document.service.DocumentRenderer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * A controller that provides access to image rendering for documents.
 */
@RestController
@RequiredArgsConstructor
public class DocumentImageController {

    private final DocumentRenderer documentRenderer;
    private final VaultClientService vaultClientService;
    private final DocumentEntityFactory documentEntityFactory;

    /**
     * Renders the first page of a document as an image. The document is queried and downloaded from the vault in the process.
     *
     * @param documentId the document to render the first page for
     * @return the first page of a document rendered as a png image
     */
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
