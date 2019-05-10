package com.github.loa.document.service;

import com.github.loa.compression.configuration.CompressionConfigurationProperties;
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
    private final CompressionConfigurationProperties compressionConfigurationProperties;

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
     * @param checksum   the checksum of the document
     */
    public void markDuplicate(final String documentId, final long fileSize, final String checksum) {
        updateFileSizeAndChecksum(documentId, fileSize, checksum);
        updateStatus(documentId, DocumentStatus.DUPLICATE);
    }

    /**
     * Mark a document's status as {@link DocumentStatus#DOWNLOADED downloaded}.
     *
     * @param documentId the id of the document to mark
     * @param fileSize   the file size of the document
     * @param checksum   the checksum of the document
     */
    public void markDownloaded(final String documentId, final long fileSize, final String checksum) {
        updateFileSizeAndChecksum(documentId, fileSize, checksum);
        updateStatus(documentId, DocumentStatus.DOWNLOADED);
        updateCompression(documentId, compressionConfigurationProperties.getAlgorithm());
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

    /**
     * Update the file size and checksum values of a document.
     *
     * @param documentId the document to update the values for
     * @param fileSize   the new file size value
     * @param checksum   the new checksum value
     */
    public void updateFileSizeAndChecksum(final String documentId, final long fileSize, final String checksum) {
        documentRepository.updateFileSizeAndChecksum(documentId, fileSize, checksum);
    }

    public void updateCompression(final String documentId, DocumentCompression documentCompression) {
        documentRepository.updateCompression(documentId, documentCompression.toString());
    }
}
