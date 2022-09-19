package com.github.bottomlessarchive.loa.stage.service;

import com.github.bottomlessarchive.loa.stage.configuration.StageConfigurationProperties;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.FileSystems;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StageLocationFactoryTest {

    private static final String TEST_LOCATION = "testlocation";
    private static final UUID TEST_DOCUMENT_ID = UUID.fromString("123e4567-e89b-42d3-a456-556642440000");

    @InjectMocks
    private StageLocationFactory underTest;

    @Mock
    private StageConfigurationProperties stageConfigurationProperties;

    @Test
    void testGetLocation() {
        when(stageConfigurationProperties.location())
                .thenReturn("testlocation");

        final StageLocation result = underTest.getLocation(TEST_DOCUMENT_ID);

        assertThat(result.getPath().toString(), is(TEST_LOCATION + FileSystems.getDefault().getSeparator()
                + TEST_DOCUMENT_ID + ".pdf"));
    }
}
