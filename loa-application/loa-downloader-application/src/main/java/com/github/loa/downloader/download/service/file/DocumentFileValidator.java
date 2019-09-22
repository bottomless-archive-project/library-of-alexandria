package com.github.loa.downloader.download.service.file;

import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.downloader.command.configuration.DownloaderConfigurationProperties;
import com.github.loa.stage.service.StageLocationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Validates a document after it's downloaded to the staging area.
 */
@Service
@RequiredArgsConstructor
public class DocumentFileValidator {

    // Anything under 1024 bytes is likely to be some bit garbage.
    private static final int MINIMUM_FILE_LENGTH = 1024;

    private final StageLocationFactory stageLocationFactory;
    private final DownloaderConfigurationProperties downloaderConfigurationProperties;

    /**
     * Evaluates a document's validity based on the document's properties in the staging area.
     *
     * @param documentId   the id of the document to validate
     * @param documentType the type of the document to validate
     */
    public boolean isValidDocument(final String documentId, final DocumentType documentType) {
        final long stageFileLocationSize = stageLocationFactory.getLocation(documentId, documentType).length();

        return stageFileLocationSize > MINIMUM_FILE_LENGTH
                && stageFileLocationSize < downloaderConfigurationProperties.getMaximumArchiveSize();
    }
}
