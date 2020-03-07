package com.github.loa.vault.service.location;

import java.io.IOException;
import java.io.InputStream;

/**
 * A location on the storage media where a document's contents are stored.
 */
public interface VaultLocation extends AutoCloseable {

    /**
     * Insert/replace the contents of the document located under this location.
     */
    void upload(final byte[] documentContents) throws IOException;

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

    @Override
    void close() throws IOException;
}
