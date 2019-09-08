package com.github.loa.vault.view.controller;

import com.github.loa.vault.service.RecompressorService;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.vault.service.VaultDocumentManager;
import com.github.loa.vault.view.request.domain.RecompressRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequiredArgsConstructor
public class VaultController {

    private final DocumentEntityFactory documentEntityFactory;
    private final VaultDocumentManager vaultDocumentManager;
    private final RecompressorService recompressorService;

    @PostMapping("/document/{documentId}")
    public void archiveDocument(@PathVariable final String documentId,
            @RequestParam("content") final MultipartFile content) throws IOException {
        final DocumentEntity documentEntity = documentEntityFactory.getDocumentEntity(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document not found with id " + documentId + "!"));

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
        final DocumentEntity documentEntity = documentEntityFactory.getDocumentEntity(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document not found with id " + documentId + "!"));

        final InputStream streamingContent = vaultDocumentManager.readDocument(documentEntity);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=" + documentId + "."
                        + documentEntity.getType().getFileExtension())
                .cacheControl(CacheControl.noCache())
                .body(new InputStreamResource(streamingContent));
    }

    @PostMapping("/document/{documentId}/recompress")
    public void recompressDocument(@PathVariable final String documentId,
            @RequestBody final RecompressRequest recompressRequest) {
        final DocumentEntity documentEntity = documentEntityFactory.getDocumentEntity(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document not found with id " + documentId + "!"));

        recompressorService.recompress(documentEntity, recompressRequest.getCompression());
    }
}
