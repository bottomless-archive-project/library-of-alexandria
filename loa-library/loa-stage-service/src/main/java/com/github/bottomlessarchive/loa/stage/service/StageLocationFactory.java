package com.github.bottomlessarchive.loa.stage.service;

import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import com.github.bottomlessarchive.loa.stage.service.domain.exception.StageAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * A factory that creates {@link java.io.File} instances in the staging area. These files could be used to save the
 * document for a short time for pre-processing before moving it into the vault.
 */
@Slf4j
@RequiredArgsConstructor
public class StageLocationFactory {

    private final Path stageDirectory;

    /**
     * Return a path in the staging area that's uniquely generated for the provided document id.
     *
     * @param documentId the document's id that we need to create the location for
     * @return the location created in the staging area
     */
    public StageLocation getLocation(final UUID documentId) {
        final Path path = buildPath(documentId);

        return StageLocation.builder()
                .path(path)
                .build();
    }

    /**
     * Return true if the staging folder has more or equal amount of free space as the required amount specified in the input parameter.
     *
     * @param requiredSpaceInBytes the amount of required free space
     * @return true if there is enough space available, false otherwise
     */
    public boolean hasSpace(final long requiredSpaceInBytes) {
        return stageDirectory.toFile().getFreeSpace() > requiredSpaceInBytes;
    }

    private Path buildPath(final UUID documentId) {
        if (!Files.exists(stageDirectory)) {
            try {
                log.info("Stage folder doesn't exists! Creating new stage folder on path: {}.", stageDirectory);

                Files.createDirectories(stageDirectory);
            } catch (final IOException e) {
                throw new StageAccessException("Unable to create non-existing stage folder!", e);
            }
        }

        return stageDirectory.resolve(buildFileName(documentId));
    }

    private String buildFileName(final UUID documentId) {
        return documentId.toString();
    }
}
