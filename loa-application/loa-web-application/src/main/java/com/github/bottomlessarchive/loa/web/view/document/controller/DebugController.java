package com.github.bottomlessarchive.loa.web.view.document.controller;

import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.web.view.document.response.DebugDocumentResponse;
import com.github.bottomlessarchive.loa.web.view.document.service.DebugResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class DebugController {

    private final DebugResponseFactory debugResponseFactory;
    private final DocumentEntityFactory documentEntityFactory;

    @GetMapping("/document/{documentId}/debug")
    public Mono<DebugDocumentResponse> getDocumentById(@PathVariable final String documentId) {
        return documentEntityFactory.getDocumentEntity(UUID.fromString(documentId))
                .flatMap(debugResponseFactory::newDebugDocumentResponse)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document not found with id " + documentId + "!")));
    }

    @GetMapping("/documents")
    public Flux<DebugDocumentResponse> getDocuments() {
        return documentEntityFactory.getDocumentEntities()
                .flatMap(debugResponseFactory::newDebugDocumentResponse);
    }
}
