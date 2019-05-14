package com.github.loa.vault.view.controller;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.vault.service.VaultDocumentManager;
import com.github.loa.vault.service.VaultLocationFactory;
import com.github.loa.vault.service.location.domain.VaultLocation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
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
    public ResponseEntity<byte[]> queryDocument(@PathVariable final String documentId) throws IOException {
        final DocumentEntity documentEntity = documentEntityFactory.getDocumentEntity(documentId);
        final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity,
                documentEntity.getCompression());

        // Can't work with pure InputStream here because we will need the decompressed file length for the response
        try (InputStream streamingContent = vaultDocumentManager.readDocument(documentEntity, vaultLocation)) {
            final byte[] documentContent = IOUtils.toByteArray(streamingContent);

            return ResponseEntity.ok()
                    .contentLength(documentContent.length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=" + documentId + "."
                            + documentEntity.getType().getFileExtension())
                    .cacheControl(CacheControl.noCache())
                    .body(documentContent);
        }
    }
}
