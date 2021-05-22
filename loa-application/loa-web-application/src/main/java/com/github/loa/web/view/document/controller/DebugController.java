package com.github.loa.web.view.document.controller;

import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.web.view.document.response.DebugDocumentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class DebugController {

    private final DocumentEntityFactory documentEntityFactory;

    @GetMapping("/document/{documentId}/debug")
    public Mono<DebugDocumentResponse> getDocumentById(@PathVariable final String documentId) {
        return documentEntityFactory.getDocumentEntity(UUID.fromString(documentId))
                .map(documentEntity -> DebugDocumentResponse.builder()
                        .id(documentEntity.getId())
                        .vault(documentEntity.getVault())
                        .type(documentEntity.getType())
                        .status(documentEntity.getStatus())
                        .compression(documentEntity.getCompression())
                        .checksum(documentEntity.getChecksum())
                        .fileSize(documentEntity.getFileSize())
                        .downloadDate(documentEntity.getDownloadDate())
                        .downloaderVersion(documentEntity.getDownloaderVersion())
                        .build()
                )
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document not found with id " + documentId + "!")));
    }
}
