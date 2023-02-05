package com.github.bottomlessarchive.loa.stage.service;

import com.github.bottomlessarchive.loa.stage.configuration.StagingConfigurationProperties;
import com.github.bottomlessarchive.loa.stage.service.location.StageAccessException;
import com.github.bottomlessarchive.loa.stage.service.location.StageLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class StageLocationFactory {

    private final StagingConfigurationProperties stagingConfigurationProperties;

    /**
     * Returns a location in the staging folder for the document with the provided id. If the staging folder doesn't exist when a call is
     * made to this function, it will be automatically created on the filesystem.
     *
     * @param documentId the id of the document to get a location for
     * @return the location for the document
     */
    public StageLocation getStageLocation(final String documentId) {
        verifyStageFolder();

        return StageLocation.builder()
                .location(stagingConfigurationProperties.location().resolve(documentId))
                .build();
    }

    /**
     * Returns the available space in the staging folder. If the staging folder doesn't exist when a call is made to this function, it will
     * be automatically created on the filesystem.
     *
     * @return the available space in the staging folder in bytes
     */
    public long getAvailableSpace() {
        verifyStageFolder();

        try {
            return Files.getFileStore(stagingConfigurationProperties.location()).getUsableSpace();
        } catch (IOException e) {
            throw new StageAccessException(e);
        }
    }

    private void verifyStageFolder() {
        final Path stageFolderPath = stagingConfigurationProperties.location();

        if (!Files.exists(stageFolderPath)) {
            try {
                log.info("Stage folder doesn't exists! Creating new stage folder on path: {}.", stageFolderPath);

                Files.createDirectories(stageFolderPath);
            } catch (final IOException e) {
                throw new StageAccessException("Unable to create non-existing vault folder!", e);
            }
        }
    }
}
