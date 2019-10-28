package com.github.loa.checksum.service;

import com.github.loa.document.service.domain.DocumentType;
import reactor.core.publisher.Mono;

/**
 * This interface is responsible for providing checksum values for documents in the stage location.
 */
public interface ChecksumProvider {

    /**
     * Generate a checksum for the provided document.
     *
     * @param documentId   the id of the document
     * @param documentType the type of the document
     * @return the checksum for the document
     */
    Mono<String> checksum(String documentId, DocumentType documentType);
}
