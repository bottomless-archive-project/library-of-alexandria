package com.github.loa.vault.view.controller;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.vault.service.VaultDocumentManager;
import com.github.loa.vault.service.VaultLocationFactory;
import com.github.loa.vault.service.location.domain.VaultLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequiredArgsConstructor
public class VaultController {

    private final DocumentEntityFactory documentEntityFactory;
    private final VaultDocumentManager vaultDocumentManager;
    private final VaultLocationFactory vaultLocationFactory;

    @PostMapping("/document/{documentId}")
    public void archiveDocument(@PathVariable final String documentId,
            @RequestParam("content") final MultipartFile content) throws IOException {
        final DocumentEntity documentEntity = documentEntityFactory.getDocumentEntity(documentId);

        vaultDocumentManager.archiveDocument(documentEntity, content.getInputStream());
    }

    /**
     * Return a document's content from the vault, based on the provided document id.
     *
     * @param documentId the id of the document to return the content for
     * @return the returned document's content
     */
    @GetMapping("/document/{documentId}")
    public ResponseEntity<InputStreamResource> queryDocument(@PathVariable final String documentId) {
        final DocumentEntity documentEntity = documentEntityFactory.getDocumentEntity(documentId);
        final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity,
                documentEntity.getCompression());

        final InputStream streamingContent = vaultDocumentManager.readDocument(documentEntity, vaultLocation);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=" + documentId + "."
                        + documentEntity.getType().getFileExtension())
                .cacheControl(CacheControl.noCache())
                .body(new InputStreamResource(streamingContent));
    }
}
