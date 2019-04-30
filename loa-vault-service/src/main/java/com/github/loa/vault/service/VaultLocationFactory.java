package com.github.loa.vault.service;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.service.location.domain.VaultLocation;

/**
 * A factory that creates {@link VaultLocation} instances for documents.
 */
public interface VaultLocationFactory {

    /**
     * Return a vault location for a document based on the provided document Id.
     *
     * @param documentId the id of the document to return the location for
     * @return the vault location belonging to the document
     */
    VaultLocation getLocation(final String documentId);

    /**
     * Return a vault location for a document.
     *
     * @param documentEntity the entity of the document to return the location for
     * @return the vault location belonging to the document
     */
    VaultLocation getLocation(final DocumentEntity documentEntity);

    VaultLocation getLocation(final DocumentEntity documentEntity, final DocumentCompression compression);

    VaultLocation getLocation(final String documentId, final DocumentCompression compression);
}
