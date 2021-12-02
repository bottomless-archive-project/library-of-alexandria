package com.github.bottomlessarchive.loa.validator.service;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import com.github.bottomlessarchive.loa.parser.service.DocumentDataParser;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import com.github.bottomlessarchive.loa.validator.configuration.FileValidationConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.UUID;

/**
 * Validates a document after it's downloaded to the staging area.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentFileValidator {

    // Anything under 1024 bytes is likely to be some bit garbage.
    private static final int MINIMUM_FILE_LENGTH = 1024;

    private final DocumentDataParser documentDataParser;
    private final StageLocationFactory stageLocationFactory;
    private final FileValidationConfigurationProperties fileValidationConfigurationProperties;

    /**
     * Evaluates a document's validity based on the document's properties in the staging area.
     *
     * @param documentId   the id of the document to validate
     * @param documentType the type of the document to validate
     */
    public Mono<Boolean> isValidDocument(final String documentId, final DocumentType documentType) {
        return stageLocationFactory.getLocation(documentId, documentType)
                .flatMap(stageFileLocation -> stageFileLocation.size()
                        .map(size -> isValidFileSize(size) && isParsable(documentId, documentType, stageFileLocation))
                );
    }

    private boolean isValidFileSize(final long stageFileSize) {
        return stageFileSize > MINIMUM_FILE_LENGTH
                && stageFileSize < fileValidationConfigurationProperties.getMaximumArchiveSize();
    }

    private boolean isParsable(final String documentId, final DocumentType documentType,
            final StageLocation stageLocation) {
        try (InputStream inputStream = stageLocation.openStream()) {
            documentDataParser.parseDocumentMetadata(UUID.fromString(documentId), documentType, inputStream);

            return true;
        } catch (final Exception e) {
            log.debug("Non-parsable document: {}!", documentId);

            return false;
        }
    }
}
