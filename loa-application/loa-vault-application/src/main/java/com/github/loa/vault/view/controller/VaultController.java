package com.github.loa.vault.view.controller;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.document.view.service.MediaTypeCalculator;
import com.github.loa.vault.service.RecompressorService;
import com.github.loa.vault.service.VaultDocumentManager;
import com.github.loa.vault.view.request.domain.RecompressRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class VaultController {

    private final DocumentEntityFactory documentEntityFactory;
    private final VaultDocumentManager vaultDocumentManager;
    private final RecompressorService recompressorService;
    private final MediaTypeCalculator mediaTypeCalculator;

    /**
     * Saves a document's content to the vault.
     *
     * @param documentId       the id of the document to save the content for
     * @param documentContents the content to save
     * @return the document entity that the content was updated for
     */
    @PostMapping("/document/{documentId}")
    public Mono<DocumentEntity> archiveDocument(@PathVariable final String documentId,
            @RequestBody final Resource documentContents) {
        return documentEntityFactory.getDocumentEntity(documentId)
                .doOnNext(documentEntity -> vaultDocumentManager.archiveDocument(documentEntity, documentContents))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document not found with id " + documentId + "!")));
    }

    /**
     * Return a document's content from the vault, based on the provided document id.
     *
     * @param documentId the id of the document to return the content for
     * @return the returned document's content
     */
    @GetMapping("/document/{documentId}")
    public Mono<ResponseEntity<Resource>> queryDocument(@PathVariable final String documentId) {
        return documentEntityFactory.getDocumentEntity(documentId)
                .map(documentEntity -> {
                    final Resource resource = vaultDocumentManager.readDocument(documentEntity);

                    return ResponseEntity.ok()
                            .contentType(mediaTypeCalculator.calculateMediaType(documentEntity.getType()))
                            .cacheControl(CacheControl.noCache())
                            .body(resource);
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document not found with id " + documentId + "!")));
    }

    /**
     * Recompress a document in the vault.
     *
     * @param documentId        the id of the document to recompress
     * @param recompressRequest the request for recompression
     * @return the document entity that the content was recompressed
     */
    @PostMapping("/document/{documentId}/recompress")
    public Mono<DocumentEntity> recompressDocument(@PathVariable final String documentId,
            @RequestBody final RecompressRequest recompressRequest) {
        return documentEntityFactory.getDocumentEntity(documentId)
                .doOnNext(documentEntity ->
                        recompressorService.recompress(documentEntity, recompressRequest.getCompression()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document not found with id " + documentId + "!")));
    }

    /**
     * Removes a document's content from the vault.
     *
     * @param documentId the document to remove the contents for
     * @return the document entity that the content was removed
     */
    @DeleteMapping("/document/{documentId}")
    public Mono<DocumentEntity> removeDocument(@PathVariable final String documentId) {
        return documentEntityFactory.getDocumentEntity(documentId)
                .doOnNext(vaultDocumentManager::removeDocument)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document not found with id " + documentId + "!")));
    }
}
