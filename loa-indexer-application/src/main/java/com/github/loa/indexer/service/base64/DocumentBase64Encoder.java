package com.github.loa.indexer.service.base64;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.service.VaultDocumentManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;

/**
 * This service is responsible for creating base64 strings from documents.
 */
@Service
@RequiredArgsConstructor
public class DocumentBase64Encoder {

    private final VaultDocumentManager vaultDocumentManager;

    /**
     * Encode a document's content to a base64 string.
     *
     * @param documentEntity the document to encode
     * @return the encoded string
     */
    public String encodeDocument(final DocumentEntity documentEntity) {
        return Base64.getEncoder().encodeToString(vaultDocumentManager.readDocument(documentEntity));
    }
}
