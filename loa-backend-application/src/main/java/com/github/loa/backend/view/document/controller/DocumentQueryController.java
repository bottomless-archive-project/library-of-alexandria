package com.github.loa.backend.view.document.controller;

import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.vault.service.VaultDocumentManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DocumentQueryController {

    private final DocumentEntityFactory documentEntityFactory;
    private final VaultDocumentManager vaultDocumentManager;

    @GetMapping("/document/{documentId}")
    public ResponseEntity<byte[]> queryDocument(@PathVariable final String documentId) {
        final byte[] documentContent = vaultDocumentManager.readDocument(
                documentEntityFactory.getDocumentEntity(documentId));

        final HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_PDF);
        responseHeaders.setCacheControl(CacheControl.noCache());

        return new ResponseEntity<>(documentContent, responseHeaders, HttpStatus.OK);
    }
}
