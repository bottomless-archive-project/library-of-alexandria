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
        updateStatus(documentId, DocumentStatus.INDEXED);
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

    /**
     * Update the compression of a document.
     *
     * @param documentId          the id of the document to update the status for
     * @param documentCompression the new compression of the document
     */
    public void updateCompression(final String documentId, final DocumentCompression documentCompression) {
        documentRepository.updateCompression(documentId, documentCompression.toString());
    }

    /**
     * Update the page count of a document.
     *
     * @param documentId the id of the document to update the page count for
     * @param pageCount  the new page count of the document
     */
    public void updatePageCount(final String documentId, final int pageCount) {
        documentRepository.updatePageCount(documentId, pageCount);
    }
}
