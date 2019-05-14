package com.github.loa.indexer.service.index.base64;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.service.VaultDocumentManager;
import com.github.loa.vault.service.VaultLocationFactory;
import com.github.loa.vault.service.location.domain.VaultLocation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;

/**
 * This service is responsible for creating base64 strings from documents.
 */
@Service
@RequiredArgsConstructor
public class DocumentBase64Encoder {

    private final VaultDocumentManager vaultDocumentManager;
    private final VaultLocationFactory vaultLocationFactory;

    /**
     * Encode a document's content to a base64 string.
     *
     * @param documentEntity the document to encode
     * @return the encoded string
     */
    public String encodeDocument(final DocumentEntity documentEntity) {
        final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity);

        try {
            //TODO: Still not satisfied that we need to read the data to memory
            return Base64.getEncoder().encodeToString(IOUtils.toByteArray(
                    vaultDocumentManager.readDocument(documentEntity, vaultLocation)));
        } catch (IOException e) {
            throw new RuntimeException("Unable to base64 encode document " + documentEntity.getId() + "!", e);
        }
    }
}
