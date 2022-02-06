package com.github.bottomlessarchive.loa.web.view.document.controller;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.document.view.service.MediaTypeCalculator;
import com.github.bottomlessarchive.loa.vault.client.service.VaultClientService;
import com.github.bottomlessarchive.loa.web.view.document.service.DocumentRenderer;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * A controller that provides access to the documents available in the vault.
 */
@RestController
@RequiredArgsConstructor
public class DocumentQueryController {

    private final DocumentRenderer documentRenderer;
    private final DocumentEntityFactory documentEntityFactory;
    private final VaultClientService vaultClientService;
    private final MediaTypeCalculator mediaTypeCalculator;

    /**
     * Return a document's content from the vault, based on the provided document id.
     *
     * @param documentId the id of the document to return the content for
     * @return the returned document's content
     */
    @GetMapping("/document/{documentId}")
    public Mono<ResponseEntity<Flux<DataBuffer>>> queryDocument(@PathVariable final String documentId) {
        return Mono.justOrEmpty(documentEntityFactory.getDocumentEntity(UUID.fromString(documentId)))
                .zipWhen(documentEntity -> Mono.just(vaultClientService.queryDocument(documentEntity)))
                .map(documentEntity -> ResponseEntity.ok()
                        .contentType(mediaTypeCalculator.calculateMediaType(documentEntity.getT1().getType()))
                        .cacheControl(CacheControl.noCache())
                        .body(documentEntity.getT2())
                )
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document not found with id " + documentId + "!")));
    }

    @GetMapping(path = "/document/{documentId}/image", produces = "image/png")
    public Mono<byte[]> queryDocumentImage(@PathVariable final String documentId) {
        return Mono.justOrEmpty(documentEntityFactory.getDocumentEntity(UUID.fromString(documentId)))
                .zipWhen(documentEntity -> Mono.just(vaultClientService.queryDocumentAsInputStream(documentEntity)))
                .flatMap(value -> {
                            if (value.getT1().getType() != DocumentType.PDF) {
                                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Images can only be rendered for documents that are in a PDF format."));
                            }

                            return value.getT2().flatMap(documentContent ->
                                    Mono.fromSupplier(() -> documentRenderer.renderFirstPage(documentContent))
                            );
                        }
                )
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document not found with id " + documentId + "!")));
    }
}
