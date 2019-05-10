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
     * Mark a document's status as {@link DocumentStatus#INDEXED proccess failure}.
     *
     * @param documentId the id of the document to mark
     */
    public void markIndexed(final String documentId) {
        updateStatus(documentId, DocumentStatus.INDEXED);
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
