package com.github.loa.document.service;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.repository.DocumentRepository;
import com.github.loa.document.service.domain.DocumentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * This service contains methods to manipulating documents.
 */
@Service
@RequiredArgsConstructor
public class DocumentManipulator {

    private final DocumentRepository documentRepository;

    /**
     * Set the document's status as {@link DocumentStatus#INDEXED indexed}.
     *
     * @param documentId the id of the document to mark as indexed
     */
    public Mono<Void> markIndexed(final UUID documentId) {
        return updateStatus(documentId, DocumentStatus.INDEXED);
    }

    /**
     * Set the document's status to {@link DocumentStatus#CORRUPT corrupt}.
     *
     * @param documentId the id of the document to mark as corrupt
     */
    public Mono<Void> markCorrupt(final UUID documentId) {
        return updateStatus(documentId, DocumentStatus.CORRUPT);
    }

    /**
     * Update the status of a document.
     *
     * @param documentId     the id of the document to update the status for
     * @param documentStatus the new status of the document
     */
    public Mono<Void> updateStatus(final UUID documentId, final DocumentStatus documentStatus) {
        return documentRepository.updateStatus(documentId, documentStatus.toString());
    }

    /**
     * Update the compression of a document.
     *
     * @param documentId          the id of the document to update the status for
     * @param documentCompression the new compression of the document
     */
    public Mono<Void> updateCompression(final UUID documentId, final DocumentCompression documentCompression) {
        return documentRepository.updateCompression(documentId, documentCompression.toString());
    }
}
