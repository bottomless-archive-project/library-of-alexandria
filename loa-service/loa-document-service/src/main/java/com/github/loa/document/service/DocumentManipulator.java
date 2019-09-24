package com.github.loa.document.service;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.repository.DocumentRepository;
import com.github.loa.document.service.domain.DocumentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
     * @param documentId the id of the document to mark as indexer
     */
    public void markIndexed(final String documentId) {
        updateStatus(documentId, DocumentStatus.INDEXING_FAILURE);
    }

    /**
     * Set the document's status to {@link DocumentStatus#INDEXING_FAILURE indexing failure}.
     *
     * @param documentId the id of the document to mark as indexer
     */
    public void markIndexFailure(final String documentId) {
        updateStatus(documentId, DocumentStatus.INDEXING_FAILURE);
    }

    /**
     * Set the document's status to {@link DocumentStatus#REMOVED removed}.
     *
     * @param documentId the id of the document to mark as removed
     */
    public void markRemoved(final String documentId) {
        updateStatus(documentId, DocumentStatus.REMOVED);
    }

    /**
     * Update the status of a document.
     *
     * @param documentId     the id of the document to update the status for
     * @param documentStatus the new status of the document
     */
    public void updateStatus(final String documentId, final DocumentStatus documentStatus) {
        documentRepository.updateStatus(documentId, documentStatus.toString());
    }

    public void updateCompression(final String documentId, DocumentCompression documentCompression) {
        documentRepository.updateCompression(documentId, documentCompression.toString());
    }
}
