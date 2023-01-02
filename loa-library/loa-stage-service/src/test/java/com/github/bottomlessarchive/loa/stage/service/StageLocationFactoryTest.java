package com.github.bottomlessarchive.loa.stage.service;

import com.github.bottomlessarchive.loa.stage.configuration.StageConfigurationProperties;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StageLocationFactoryTest {

    private static final UUID TEST_DOCUMENT_ID = UUID.fromString("123e4567-e89b-42d3-a456-556642440000");

    private StageLocationFactory underTest;

    @Mock
    private Path stageLocation;

    @BeforeEach
    void setup() {
        underTest = new StageLocationFactory(
                new StageConfigurationProperties(stageLocation)
        );
    }

    @Test
    void testGetLocation() {
        final Path mockPath = mock(Path.class);
        when(stageLocation.resolve("123e4567-e89b-42d3-a456-556642440000"))
                .thenReturn(mockPath);

        final StageLocation result = underTest.getLocation(TEST_DOCUMENT_ID);

        assertThat(result.getPath())
                .isSameAs(mockPath);
    }
}
