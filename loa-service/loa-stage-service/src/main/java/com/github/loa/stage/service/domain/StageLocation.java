package com.github.loa.stage.service.domain;

import com.github.loa.stage.service.domain.exception.StageAccessException;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Getter
@Builder
public class StageLocation {

    private final Path path;

    public boolean exists() {
        return Files.exists(path);
    }

    public long size() {
        try {
            return Files.size(path);
        } catch (final IOException e) {
            throw new StageAccessException("Unable to get the size of staged document!", e);
        }
    }

    public InputStream openStream() {
        try {
            return Files.newInputStream(path);
        } catch (final IOException e) {
            throw new StageAccessException("Unable to get the input stream for staged document!", e);
        }
    }

    public void cleanup() {
        try {
            Files.delete(path);
        } catch (final IOException e) {
            //TODO: This should throw an exception as well!
            log.error("Unable to delete staged document!", e);
        }
    }
}
