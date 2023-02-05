package com.github.bottomlessarchive.loa.stage.service;

import com.github.bottomlessarchive.loa.stage.configuration.StagingConfigurationProperties;
import com.github.bottomlessarchive.loa.stage.service.location.StageLocation;
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

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class StageLocationFactoryTest {

    private StageLocationFactory underTest;

    private FileSystem fileSystem;

    @BeforeEach
    void setup() {
        fileSystem = Jimfs.newFileSystem(
                Configuration.unix().toBuilder()
                        .setMaxSize(50000)
                        .build()
        );

        underTest = new StageLocationFactory(
                new StagingConfigurationProperties(fileSystem.getPath("/stage"))
        );
    }

    @AfterEach
    void teardown() throws IOException {
        fileSystem.close();
    }

    @Test
    void testGetStageLocation() throws IOException {
        Files.createDirectories(fileSystem.getPath("/stage"));

        final StageLocation result = underTest.getStageLocation("123");

        assertThat(result.location())
                .isEqualTo(fileSystem.getPath("/stage/123"));
    }

    @Test
    void testGetStageLocationWhenFolderDoesntExists() {
        final StageLocation result = underTest.getStageLocation("123");

        assertThat(result.location())
                .isEqualTo(fileSystem.getPath("/stage/123"));
        assertThat(fileSystem.getPath("/stage"))
                .exists();
    }

    @Test
    void testGetAvailableSpace() throws IOException {
        Files.createDirectories(fileSystem.getPath("/stage"));

        final long result = underTest.getAvailableSpace();

        assertThat(result)
                .isEqualTo(49152L);
    }

    @Test
    void testGetAvailableSpaceWhenFolderDoesntExists() {
        final long result = underTest.getAvailableSpace();

        assertThat(result)
                .isEqualTo(49152L);
        assertThat(fileSystem.getPath("/stage"))
                .exists();
    }
}
