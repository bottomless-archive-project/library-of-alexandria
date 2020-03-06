package com.github.loa.vault.service.location;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A location on the storage media where a document's contents are stored.
 */
public interface VaultLocation extends AutoCloseable {

    /**
     * Return an output stream that points to the space on the storage media where the document's content are archived.
     * Should be used if you want to modify/replace the content of the document. The caller of this method is
     * responsible for closing the returned stream.
     *
     * @return the location for the document's content
     */
    OutputStream destination();

    /**
     * Get the content of the location as an input stream. The caller of this method is responsible for closing the
     * returned stream.
     *
     * @return the content of the location
     */
    InputStream content();

    /**
     * Removes any previously stored data from the location.
     */
    void clear();

    @Override
    void close() throws IOException;
}
