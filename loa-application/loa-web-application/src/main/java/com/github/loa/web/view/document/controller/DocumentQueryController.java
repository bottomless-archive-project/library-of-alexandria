package com.github.loa.web.view.document.controller;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.vault.service.VaultDocumentManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * A controller that provides access to the documents available in the vault.
 */
@RestController
@RequiredArgsConstructor
public class DocumentQueryController {

    private final DocumentEntityFactory documentEntityFactory;
    private final VaultDocumentManager vaultDocumentManager;

    /**
     * Return a document's content from the vault, based on the provided document id.
     *
     * @param documentId the id of the document to return the content for
     * @return the returned document's content
     */
    @GetMapping("/document/{documentId}")
    public ResponseEntity<byte[]> queryDocument(@PathVariable final String documentId) {
        final DocumentEntity documentEntity = documentEntityFactory.getDocumentEntity(documentId);
        final byte[] documentContent = vaultDocumentManager.readDocument(documentEntity);

        final HttpHeaders responseHeaders = new HttpHeaders();
        //TODO: Create a service that's able to figure the MediaType out for a DocumentType. Add other types.
        if (documentEntity.getType() == DocumentType.PDF) {
            responseHeaders.setContentType(MediaType.APPLICATION_PDF);
        } else if (documentEntity.getType() == DocumentType.DOC) {
            responseHeaders.setContentType(MediaType.valueOf("application/msword"));
        }
        responseHeaders.setCacheControl(CacheControl.noCache());

        return new ResponseEntity<>(documentContent, responseHeaders, HttpStatus.OK);
    }
}
