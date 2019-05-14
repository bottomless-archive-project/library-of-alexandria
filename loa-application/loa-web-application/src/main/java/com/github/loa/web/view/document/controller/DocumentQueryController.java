package com.github.loa.web.view.document.controller;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.vault.service.VaultDocumentManager;
import com.github.loa.web.view.document.service.MediaTypeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final MediaTypeCalculator mediaTypeCalculator;

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
        responseHeaders.setContentType(mediaTypeCalculator.calculateMediaType(documentEntity.getType()));
        responseHeaders.setCacheControl(CacheControl.noCache());

        return new ResponseEntity<>(documentContent, responseHeaders, HttpStatus.OK);
    }
}
