package com.github.loa.checksum.service;

import reactor.core.publisher.Mono;

import java.nio.file.Path;

/**
 * This interface is responsible for providing checksum values for documents in the stage location.
 */
public interface ChecksumProvider {

    /**
     * Generate a checksum for the provided document.
     *
     * @param documentId       the id of the document
     * @param documentContents the contents of the document
     * @return the checksum for the document
     */
    Mono<String> checksum(String documentId, Path documentContents);
}
