package com.github.bottomlessarchive.loa.document.service;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.document.repository.DocumentRepository;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * This service contains methods to manipulating documents.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentManipulator {

    private final DocumentRepository documentRepository;

    /**
     * Set the document's status as {@link DocumentStatus#DOWNLOADED downloaded}.
     *
     * @param documentId the id of the document to mark as downloaded
     */
    public void markDownloaded(final UUID documentId) {
        updateStatus(documentId, DocumentStatus.DOWNLOADED);
    }

    /**
     * Set the document's status as {@link DocumentStatus#INDEXED indexed}.
     *
     * @param documentId the id of the document to mark as indexed
     */
    public void markIndexed(final UUID documentId) {
        updateStatus(documentId, DocumentStatus.INDEXED);
    }

    /**
     * Set the document's status to {@link DocumentStatus#CORRUPT corrupt}.
     *
     * @param documentId the id of the document to mark as corrupt
     */
    public void markCorrupt(final UUID documentId) {
        updateStatus(documentId, DocumentStatus.CORRUPT);
    }

    /**
     * Update the status of a document.
     *
     * @param documentId     the id of the document to update the status for
     * @param documentStatus the new status of the document
     */
    public void updateStatus(final UUID documentId, final DocumentStatus documentStatus) {
        log.debug("Updating status of document with id: {} to: {}.", documentId, documentStatus);

        documentRepository.updateStatus(documentId, documentStatus.toString());
    }

    /**
     * Update the compression of a document.
     *
     * @param documentId          the id of the document to update the status for
     * @param documentCompression the new compression of the document
     */
    public void updateCompression(final UUID documentId, final DocumentCompression documentCompression) {
        documentRepository.updateCompression(documentId, documentCompression.toString());
    }
}
