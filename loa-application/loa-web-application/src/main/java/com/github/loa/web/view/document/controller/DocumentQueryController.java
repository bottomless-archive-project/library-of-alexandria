package com.github.loa.web.view.document.controller;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.vault.service.VaultDocumentManager;
import com.github.loa.vault.service.VaultLocationFactory;
import com.github.loa.vault.service.location.domain.VaultLocation;
import com.github.loa.web.view.document.service.MediaTypeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;

/**
 * A controller that provides access to the documents available in the vault.
 */
@RestController
@RequiredArgsConstructor
public class DocumentQueryController {

    private final DocumentEntityFactory documentEntityFactory;
    private final VaultDocumentManager vaultDocumentManager;
    private final VaultLocationFactory vaultLocationFactory;
    private final MediaTypeCalculator mediaTypeCalculator;

    /**
     * Return a document's content from the vault, based on the provided document id.
     *
     * @param documentId the id of the document to return the content for
     * @return the returned document's content
     */
    @GetMapping("/document/{documentId}")
    public ResponseEntity<InputStreamResource> queryDocument(@PathVariable final String documentId) throws IOException {
        final DocumentEntity documentEntity = documentEntityFactory.getDocumentEntity(documentId);
        final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity);

        try (InputStream streamingContent = vaultDocumentManager.readDocument(documentEntity, vaultLocation)) {
            return ResponseEntity.ok()
                    .contentType(mediaTypeCalculator.calculateMediaType(documentEntity.getType()))
                    .header("Content-Disposition", "attachment; filename=" + documentId + "."
                            + documentEntity.getType().getFileExtension())
                    .cacheControl(CacheControl.noCache())
                    .body(new InputStreamResource(streamingContent));
        }
    }
}
