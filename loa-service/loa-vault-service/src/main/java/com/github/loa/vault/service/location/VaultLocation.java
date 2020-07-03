package com.github.loa.vault.service.location;

import java.io.InputStream;

/**
 * A location on the storage media where a document's contents are stored.
 */
public interface VaultLocation {

    /**
     * Insert/replace the contents of the document located under this location.
     */
    void upload(final byte[] documentContents);

    /**
     * Get the content of the location.
     *
     * @return the content of the location
     */
    InputStream download();

    /**
     * Removes any previously stored data from the location.
     */
    void clear();
}
