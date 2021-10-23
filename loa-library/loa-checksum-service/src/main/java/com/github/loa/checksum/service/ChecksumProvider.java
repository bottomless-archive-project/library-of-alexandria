package com.github.loa.checksum.service;

import reactor.core.publisher.Mono;

/**
 * This interface is responsible for providing checksum values for documents in the stage location.
 */
public interface ChecksumProvider {

    /**
     * Generate a checksum for the provided document.
     *
     * @param documentContents the contents of the document
     * @return the checksum for the document
     */
    Mono<String> checksum(byte[] documentContents);
}
