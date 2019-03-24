package com.github.loa.document.service;

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
     * Mark a document's status as {@link DocumentStatus#FAILED failed}.
     *
     * @param documentId the id of the document to mark
     */
    public void markFailed(final String documentId) {
        updateStatus(documentId, DocumentStatus.FAILED);
    }

    /**
     * Mark a document's status as {@link DocumentStatus#INVALID invalid}.
     *
     * @param documentId the id of the document to mark
     */
    public void markInvalid(final String documentId) {
        updateStatus(documentId, DocumentStatus.INVALID);
    }

    /**
     * Mark a document's status as {@link DocumentStatus#DUPLICATE duplicate}.
     *
     * @param documentId the id of the document to mark
     * @param fileSize   the file size of the document
     * @param crc        the crc of the document
     */
    public void markDuplicate(final String documentId, final long fileSize, final String crc) {
        updateFileSizeAndCrc(documentId, fileSize, crc);
        updateStatus(documentId, DocumentStatus.DUPLICATE);
    }

    /**
     * Mark a document's status as {@link DocumentStatus#DOWNLOADED downloaded}.
     *
     * @param documentId the id of the document to mark
     * @param fileSize   the file size of the document
     * @param crc        the crc of the document
     */
    public void markDownloaded(final String documentId, final long fileSize, final String crc) {
        updateFileSizeAndCrc(documentId, fileSize, crc);
        updateStatus(documentId, DocumentStatus.DOWNLOADED);
    }

    /**
     * Mark a document's status as {@link DocumentStatus#PROCESS_FAILURE proccess failure}.
     *
     * @param documentId the id of the document to mark
     */
    public void markProcessFailure(final String documentId) {
        updateStatus(documentId, DocumentStatus.PROCESS_FAILURE);
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
     * Update the file size and crc values of a document.
     *
     * @param documentId the document to update the values for
     * @param fileSize   the new file size value
     * @param crc        the new crc value
     */
    public void updateFileSizeAndCrc(final String documentId, final long fileSize, final String crc) {
        documentRepository.updateFileSizeAndCrc(documentId, fileSize, crc);
    }
}
