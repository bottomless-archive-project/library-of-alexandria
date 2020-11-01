package com.github.loa.vault.service.location;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.vault.domain.exception.StorageAccessException;

import java.io.InputStream;
import java.util.Optional;

/**
 * A location on the storage media where a document's contents are stored.
 */
public interface VaultLocation {

    /**
     * Return an output stream that points to the space where the document's content are archived. Should be used if
     * you want to modify the content of the document.
     *
     * @throws StorageAccessException when unable to create the destination file
     */
    void upload(byte[] documentContents);

    /**
     * Return the content of the document in the vault.
     *
     * @return the content of the file
     */
    InputStream download();

    /**
     * Removes any previously stored data from the document.
     */
    void clear();

    /**
     * Return the compression of the document on the location.
     *
     * @return the compression of the document on the location
     */
    Optional<DocumentCompression> getCompression();
}
