package com.github.loa.vault.view.controller;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.vault.service.RecompressorService;
import com.github.loa.vault.service.VaultDocumentManager;
import com.github.loa.vault.view.request.domain.RecompressRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @PostMapping("/document/{documentId}")
    public Mono<DocumentEntity> archiveDocument(@PathVariable final String documentId,
            @RequestBody final Resource requestBody) {
        return documentEntityFactory.getDocumentEntity(documentId)
                .doOnNext(documentEntity -> vaultDocumentManager.archiveDocument(documentEntity, requestBody))
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
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .header("Content-Disposition", "inline; filename="
                                    + documentId + "." + documentEntity.getType().getFileExtension())
                            .cacheControl(CacheControl.noCache())
                            .body(resource);
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document not found with id " + documentId + "!")));
    }

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
