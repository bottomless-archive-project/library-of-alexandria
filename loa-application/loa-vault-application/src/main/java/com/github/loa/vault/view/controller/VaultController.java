package com.github.loa.vault.view.controller;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.vault.service.RecompressorService;
import com.github.loa.vault.service.VaultDocumentManager;
import com.github.loa.vault.view.request.domain.RecompressRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequiredArgsConstructor
public class VaultController {

    private final DocumentEntityFactory documentEntityFactory;
    private final VaultDocumentManager vaultDocumentManager;
    private final RecompressorService recompressorService;

    @PostMapping("/document/{documentId}")
    public Mono<DocumentEntity> archiveDocument(@PathVariable final String documentId,
            @RequestParam("content") final MultipartFile content) throws IOException {
        final InputStream contentInputStream = content.getInputStream();

        return documentEntityFactory.getDocumentEntity(documentId)
                .doOnNext(documentEntity -> vaultDocumentManager.archiveDocument(documentEntity, contentInputStream))
                .switchIfEmpty(Mono.just(DocumentEntity.builder().build()));
        //new ResponseStatusException(HttpStatus.NOT_FOUND,
        //                                "Document not found with id " + documentId + "!");
    }

    /**
     * Return a document's content from the vault, based on the provided document id.
     *
     * @param documentId the id of the document to return the content for
     * @return the returned document's content
     */
    @GetMapping("/document/{documentId}")
    public ResponseEntity<InputStreamSource> queryDocument(@PathVariable final String documentId) {
        final DocumentEntity documentEntity = documentEntityFactory.getDocumentEntity(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document not found with id " + documentId + "!"));

        final Resource resource = vaultDocumentManager.readDocument(documentEntity);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=" + documentId + "."
                        + documentEntity.getType().getFileExtension())
                .cacheControl(CacheControl.noCache())
                .body(resource);
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
