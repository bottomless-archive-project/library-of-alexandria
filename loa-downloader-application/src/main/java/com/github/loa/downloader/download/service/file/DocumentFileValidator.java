package com.github.loa.downloader.download.service.file;

import com.github.loa.stage.service.StageLocationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * Validates a document after download.
 */
@Service
@RequiredArgsConstructor
public class DocumentFileValidator {

    private final StageLocationFactory stageLocationFactory;

    /**
     * Evaluates a document's validity based on the document's file in the staging area.
     *
     * @param documentId the id of the document to validate
     */
    public boolean isValidDocument(final String documentId) {
        final File stageFileLocation = stageLocationFactory.getLocation(documentId);

        // Anything under 1024 bytes is likely to be some bit garbage.
        return stageFileLocation.length() > 1024;
    }
}
