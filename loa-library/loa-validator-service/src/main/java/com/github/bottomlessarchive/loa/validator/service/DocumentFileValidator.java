package com.github.bottomlessarchive.loa.validator.service;

import com.github.bottomlessarchive.loa.parser.service.DocumentDataParser;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.bottomlessarchive.loa.validator.configuration.FileValidationConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private final FileValidationConfigurationProperties fileValidationConfigurationProperties;

    /**
     * Evaluates a document's validity based on the document's properties in the staging area.
     *
     * @param documentId   the id of the document to validate
     * @param documentType the type of the document to validate
     */
    public boolean isValidDocument(final UUID documentId, final StageLocation stageLocation, final DocumentType documentType) {
        //TODO: StageLocation should extend and interface called FileLocation. This module shouldn't know about stage at all!
        return stageLocation.exists() && isValidFileSize(stageLocation.size()) && isParsable(documentId, documentType, stageLocation);
    }

    private boolean isValidFileSize(final long stageFileSize) {
        return stageFileSize > MINIMUM_FILE_LENGTH
                && stageFileSize < fileValidationConfigurationProperties.maximumArchiveSize();
    }

    private boolean isParsable(final UUID documentId, final DocumentType documentType,
            final StageLocation stageLocation) {
        try (InputStream inputStream = stageLocation.openStream()) {
            documentDataParser.parseDocumentMetadata(documentId, documentType, inputStream);

            return true;
        } catch (final Exception e) {
            log.debug("Non-parsable document: {}!", documentId);

            return false;
        }
    }
}
