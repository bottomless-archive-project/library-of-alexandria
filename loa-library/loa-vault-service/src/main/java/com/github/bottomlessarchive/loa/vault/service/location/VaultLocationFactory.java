package com.github.bottomlessarchive.loa.vault.service.location;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;

/**
 * A factory that creates {@link VaultLocation} instances for documents.
 */
public interface VaultLocationFactory {

    /**
     * Return a vault location for a document.
     *
     * @param documentEntity the entity of the document to return the location for
     * @return the vault location belonging to the document
     */
    default VaultLocation getLocation(final DocumentEntity documentEntity) {
        return getLocation(documentEntity, documentEntity.getCompression());
    }

    /**
     * Return a vault location for a document. The filename part of the location is calculated using the provided
     * compression.
     *
     * @param documentEntity the entity of the document to return the location for
     * @param compression    the compression used in the location calculation
     * @return the vault location belonging to the document
     */
    VaultLocation getLocation(DocumentEntity documentEntity, DocumentCompression compression);

    /**
     * Return the available free space in bytes on the location host.
     *
     * @return the free bytes available
     */
    long getAvailableSpace();
}
