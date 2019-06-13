package com.github.loa.vault.service;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.service.location.domain.VaultLocation;

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
    VaultLocation getLocation(final DocumentEntity documentEntity);

    /**
     * Return a vault location for a document. The filename part of the location is calculated using the provided
     * compression.
     *
     * @param documentEntity the entity of the document to return the location for
     * @param compression    the compression used in the location calculation
     * @return the vault location belonging to the document
     */
    VaultLocation getLocation(final DocumentEntity documentEntity, final DocumentCompression compression);
}
