package com.github.loa.vault.view.controller;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.vault.service.VaultDocumentManager;
import com.github.loa.vault.service.VaultLocationFactory;
import com.github.loa.vault.service.location.domain.VaultLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

@RestController
@RequiredArgsConstructor
public class VaultController {

    private final DocumentEntityFactory documentEntityFactory;
    private final VaultDocumentManager vaultDocumentManager;
    private final VaultLocationFactory vaultLocationFactory;

    /**
     * Return a document's content from the vault, based on the provided document id.
     *
     * @param documentId the id of the document to return the content for
     * @return the returned document's content
     */
    @GetMapping("/document/{documentId}")
    public ResponseEntity<InputStreamResource> queryDocument(@PathVariable final String documentId) {
        final DocumentEntity documentEntity = documentEntityFactory.getDocumentEntity(documentId);
        final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity);

        final InputStream documentContent = vaultDocumentManager.readDocument(documentEntity, vaultLocation);

        return ResponseEntity.ok()
                .contentLength(vaultLocation.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .cacheControl(CacheControl.noCache())
                .body(new InputStreamResource(documentContent));
    }
}
