package com.github.bottomlessarchive.loa.web.view.document.controller;

import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.web.view.document.response.DocumentDebugResponse;
import com.github.bottomlessarchive.loa.web.view.document.service.DocumentDebugResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DocumentDebugController {

    private final DocumentEntityFactory documentEntityFactory;
    private final DocumentDebugResponseFactory documentDebugResponseFactory;

    @GetMapping("/document/{documentId}/debug")
    public DocumentDebugResponse getDocumentById(@PathVariable final String documentId) {
        return documentEntityFactory.getDocumentEntity(UUID.fromString(documentId.trim()))
                .map(documentDebugResponseFactory::newDocumentDebugResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document not found with id " + documentId + "!"));
    }
}
