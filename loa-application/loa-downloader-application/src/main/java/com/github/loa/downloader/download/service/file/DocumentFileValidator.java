package com.github.loa.downloader.download.service.file;

import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.downloader.command.configuration.DownloaderConfigurationProperties;
import com.github.loa.stage.service.StageLocationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;

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
    public Mono<Boolean> isValidDocument(final String documentId, final DocumentType documentType) {
        return stageLocationFactory.getLocation(documentId, documentType)
                .map(File::length)
                .map(stageFileLocationSize -> stageFileLocationSize > MINIMUM_FILE_LENGTH
                        && stageFileLocationSize < downloaderConfigurationProperties.getMaximumArchiveSize());
    }
}
