package com.github.bottomlessarchive.loa.stage.service;

import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class StageLocationFactoryTest {

    private static final UUID TEST_DOCUMENT_ID = UUID.fromString("123e4567-e89b-42d3-a456-556642440000");

    private Path stageLocation;
    private StageLocationFactory underTest;
    private FileSystem fileSystem;

    @BeforeEach
    void setup() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());

        stageLocation = fileSystem.getPath("/stage/location");

        underTest = new StageLocationFactory(stageLocation);
    }

    @AfterEach
    void teardown() throws IOException {
        fileSystem.close();
    }

    @Test
    void testGetLocationWhenLocationExists() throws IOException {
        Files.createDirectories(stageLocation);

        final StageLocation result = underTest.getLocation(TEST_DOCUMENT_ID);

        assertThat(result.getPath())
                .isEqualTo(fileSystem.getPath("/stage/location/123e4567-e89b-42d3-a456-556642440000"));
    }

    @Test
    void testGetLocationWhenLocationDoesNotExists() {
        final StageLocation result = underTest.getLocation(TEST_DOCUMENT_ID);

        assertThat(result.getPath())
                .isEqualTo(fileSystem.getPath("/stage/location/123e4567-e89b-42d3-a456-556642440000"));
        assertThat(fileSystem.getPath("/stage/location/"))
                .exists();
    }
}
