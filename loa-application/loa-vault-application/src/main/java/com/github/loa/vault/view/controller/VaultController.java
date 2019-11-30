package com.github.loa.vault.view.controller;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.document.view.service.MediaTypeCalculator;
import com.github.loa.stage.service.StageLocationFactory;
import com.github.loa.vault.service.RecompressorService;
import com.github.loa.vault.service.VaultDocumentManager;
import com.github.loa.vault.service.domain.DocumentArchivingContext;
import com.github.loa.vault.view.request.ArchiveDocumentRequest;
import com.github.loa.vault.view.request.domain.RecompressRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class VaultController {

    private final DocumentEntityFactory documentEntityFactory;
    private final VaultDocumentManager vaultDocumentManager;
    private final RecompressorService recompressorService;
    private final MediaTypeCalculator mediaTypeCalculator;
    private final StageLocationFactory stageLocationFactory;

    /**
     * Saves a document's content to the vault.
     *
     * @param archiveDocumentRequest the content to save
     * @return the document entity that the content was updated for
     */
    @PostMapping("/document")
    public Mono<DocumentEntity> archiveDocument(
            @RequestPart(value = "document") final ArchiveDocumentRequest archiveDocumentRequest,
            @RequestPart(value = "contents") final FilePart documentContents) {
        final String documentId = UUID.randomUUID().toString();

        return stageLocationFactory.getLocation(documentId, archiveDocumentRequest.getType())
                .flatMap(stageLocation -> documentContents.transferTo(stageLocation.getPath())
                        .map(result -> DocumentArchivingContext.builder()
                                .type(archiveDocumentRequest.getType())
                                .location(archiveDocumentRequest.getLocation())
                                .source(archiveDocumentRequest.getSource())
                                .contents(stageLocation.getPath())
                                .build()
                        )
                        .flatMap(vaultDocumentManager::archiveDocument)
                        .doOnTerminate(stageLocation::cleanup)
                )
                .subscribeOn(Schedulers.boundedElastic());
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
