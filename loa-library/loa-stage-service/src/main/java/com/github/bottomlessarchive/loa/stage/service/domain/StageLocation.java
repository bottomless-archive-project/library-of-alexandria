package com.github.bottomlessarchive.loa.stage.service.domain;

import com.github.bottomlessarchive.loa.stage.service.domain.exception.StageAccessException;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A location that could be written in the staging area.
 */
@Slf4j
@Getter
@Builder
public class StageLocation {

    private final Path path;

    /**
     * Return if the stage location already exists.
     *
     * @return true if the location exists, false otherwise
     */
    public Mono<Boolean> exists() {
        return Mono.fromSupplier(() -> Files.exists(path));
    }

    /**
     * Return the size of the content at the stage location.
     *
     * @return the file size at the stage location
     */
    public long size() {
        try {
            return Files.size(path);
        } catch (final IOException e) {
            throw new StageAccessException("Unable to get the size of staged document!", e);
        }
    }

    /**
     * Return an {@link InputStream} to the contents at the stage location.
     *
     * @return input stream to the content
     */
    public InputStream openStream() {
        try {
            return Files.newInputStream(path);
        } catch (final IOException e) {
            throw new StageAccessException("Unable to get the input stream for staged document!", e);
        }
    }

    /**
     * Removes the content of the stage location.
     *
     * @return result of the operation
     */
    public void cleanup() {
        try {
            Files.delete(path);
        } catch (final IOException e) {
            throw new StageAccessException("Unable to delete staged document!", e);
        }
    }
}
