package com.github.loa.vault.service.backend.service;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.service.location.VaultLocation;

import java.io.InputStream;

/**
 * The document storage is responsible to store and later retrieve the content of a document.
 */
public interface VaultDocumentStorage {

    /**
     * Store the document contents.
     *
     * @param documentEntity   the document to store the contents for
     * @param documentContents the contents of the document
     */
    void persistDocument(DocumentEntity documentEntity, InputStream documentContents);

    /**
     * Store the document contents on the provided {@link VaultLocation}.
     *
     * @param documentEntity   the document to store the contents for
     * @param documentContents the contents of the document
     * @param vaultLocation    the vault location where the document's contents should be stored
     */
    void persistDocument(DocumentEntity documentEntity, InputStream documentContents, VaultLocation vaultLocation);
}
