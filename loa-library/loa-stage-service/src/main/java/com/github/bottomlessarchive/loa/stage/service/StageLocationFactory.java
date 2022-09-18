package com.github.bottomlessarchive.loa.stage.service;

import com.github.bottomlessarchive.loa.stage.configuration.StageConfigurationProperties;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.UUID;

/**
 * A factory that creates {@link java.io.File} instances in the staging area. These files could be used to save the
 * document for a short time for pre-processing before moving it into the vault.
 */
@Service
@RequiredArgsConstructor
public class StageLocationFactory {

    private final StageConfigurationProperties stageConfigurationProperties;

    /**
     * Return a path in the staging area that's uniquely generated for the provided document id.
     *
     * @param documentId   the document's id that we need to create the location for
     * @param documentType the type of the document that we need to create the location for
     * @return the location created in the staging area
     */
    public StageLocation getLocation(final UUID documentId, final DocumentType documentType) {
        final Path path = Path.of(stageConfigurationProperties.location(), buildFileName(documentId, documentType));

        return StageLocation.builder()
                .path(path)
                .build();
    }

    public boolean hasSpace(final long requiredSpaceInBytes) {
        return Path.of(stageConfigurationProperties.location()).toFile().getFreeSpace() > requiredSpaceInBytes;
    }

    private String buildFileName(final UUID documentId, final DocumentType documentType) {
        return documentId + "." + documentType.getFileExtension();
    }
}
