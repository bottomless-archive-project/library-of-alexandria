package com.github.loa.indexer.service.index.base64;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.client.service.VaultClientService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * This service is responsible for creating base64 strings from documents.
 */
@Service
@RequiredArgsConstructor
public class DocumentBase64Encoder {

    private final VaultClientService vaultClientService;

    /**
     * Encode a document's content to a base64 string.
     *
     * @param documentEntity the document to encode
     * @return the encoded string
     */
    public String encodeDocument(final DocumentEntity documentEntity) {
        final InputStream inputStream = vaultClientService.getVaultContent(documentEntity);

        try {
            //TODO: Still not satisfied that we need to read the data to memory
            return Base64.getEncoder().encodeToString(IOUtils.toByteArray(inputStream));
        } catch (IOException e) {
            throw new RuntimeException("Unable to base64 encode document " + documentEntity.getId() + "!", e);
        }
    }
}
