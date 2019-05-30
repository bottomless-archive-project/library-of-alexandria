package com.github.loa.indexer.service.index.base64;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.client.service.VaultClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * This service is responsible for creating base64 strings from documents.
 */
@Service
@RequiredArgsConstructor
public class DocumentBase64Encoder {

    private final Base64Encoder base64Encoder;
    private final VaultClientService vaultClientService;

    /**
     * Encode a document's content to a base64 string.
     *
     * @param documentEntity the document to encode
     * @return the encoded string
     */
    public String encodeDocument(final DocumentEntity documentEntity) {
        final InputStream inputStream = vaultClientService.queryDocument(documentEntity);

        return base64Encoder.encode(inputStream);
    }
}
