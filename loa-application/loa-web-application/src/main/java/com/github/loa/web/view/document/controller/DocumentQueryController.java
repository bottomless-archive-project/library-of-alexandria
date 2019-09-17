package com.github.loa.web.view.document.controller;

import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.vault.client.service.VaultClientService;
import com.github.loa.web.view.document.service.MediaTypeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.InputStream;

/**
 * A controller that provides access to the documents available in the vault.
 */
@RestController
@RequiredArgsConstructor
public class DocumentQueryController {

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
    public Mono<ResponseEntity<InputStreamResource>> queryDocument(@PathVariable final String documentId) {
        return documentEntityFactory.getDocumentEntity(documentId)
                .map(documentEntity -> {
                    final InputStream streamingContent = vaultClientService.queryDocument(documentEntity);

                    final HttpHeaders httpHeaders = new HttpHeaders();

                    if (documentEntity.getType() != DocumentType.PDF) {
                        httpHeaders.setContentDisposition(
                                ContentDisposition.builder("attachment")
                                        .filename(documentId + "." + documentEntity.getType().getFileExtension())
                                        .build()
                        );
                    }

                    return ResponseEntity.ok()
                            .headers(httpHeaders)
                            .contentType(mediaTypeCalculator.calculateMediaType(documentEntity.getType()))
                            .cacheControl(CacheControl.noCache())
                            .body(new InputStreamResource(streamingContent));
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document not found with id " + documentId + "!")));
    }
}
