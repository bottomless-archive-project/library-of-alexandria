package com.github.loa.vault.view.controller;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.document.view.service.MediaTypeCalculator;
import com.github.loa.vault.service.RecompressorService;
import com.github.loa.vault.service.VaultDocumentManager;
import com.github.loa.vault.service.domain.DocumentArchivingContext;
import com.github.loa.vault.view.request.ArchiveDocumentRequest;
import com.github.loa.vault.view.request.domain.RecompressRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
     * @param archiveDocumentRequest the content to save
     * @return the document entity that the content was updated for
     */
    @PostMapping("/document")
    public Mono<DocumentEntity> archiveDocument(
            @RequestPart(value = "document") final ArchiveDocumentRequest archiveDocumentRequest,
            @RequestPart(value = "contents") final Resource documentContents) {
        final DocumentArchivingContext documentArchivingContext = DocumentArchivingContext.builder()
                .type(archiveDocumentRequest.getType())
                .location(archiveDocumentRequest.getLocation())
                .source(archiveDocumentRequest.getSource())
                .contents(documentContents)
                .build();

        return vaultDocumentManager.archiveDocument(documentArchivingContext)
                .subscribeOn(Schedulers.parallel());
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
                        "Document not found with id " + documentId + " or already removed!")));
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
}
