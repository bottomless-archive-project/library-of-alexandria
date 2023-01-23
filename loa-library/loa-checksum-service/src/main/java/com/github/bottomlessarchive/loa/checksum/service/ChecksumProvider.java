package com.github.bottomlessarchive.loa.checksum.service;

import java.io.InputStream;

/**
 * This interface is responsible for providing checksum values for documents in the stage location.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Checksum">https://en.wikipedia.org/wiki/Checksum</a>
 */
public interface ChecksumProvider {

    /**
     * Generate a checksum for the provided document.
     *
     * @param documentContents the contents of the document
     * @return the checksum for the document
     */
    String checksum(InputStream documentContents);
}
