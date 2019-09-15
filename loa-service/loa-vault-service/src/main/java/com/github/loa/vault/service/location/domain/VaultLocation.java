package com.github.loa.vault.service.location.domain;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public interface VaultLocation extends Closeable {

    /**
     * Return an output stream that points to the space where the document's content are archived. Should be used if
     * you want to modify the content of the document.
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
     * Get the content of the location as a {@link File}.
     *
     * @return the content of the location
     */
    File file();

    /**
     * Removes any previously stored data from the location.
     */
    void clear();
}
