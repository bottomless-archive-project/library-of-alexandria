package com.github.bottomlessarchive.loa.downloader.service;

import com.github.bottomlessarchive.loa.downloader.configuration.DownloaderConfigurationProperties;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.location.service.factory.domain.DocumentLocationCreationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentLocationCreationContextFactoryTest {

    @Mock
    private DownloaderConfigurationProperties downloaderConfigurationProperties;

    @InjectMocks
    private DocumentLocationCreationContextFactory documentLocationCreationContextFactory;

    @Test
    void testNewCreatingContext() {
        final DocumentLocation documentLocation = DocumentLocation.builder()
                .id("test-id")
                .sourceName("test-source")
                .location("http://test-url/")
                .build();
        when(downloaderConfigurationProperties.versionNumber())
                .thenReturn(5);

        final DocumentLocationCreationContext result = documentLocationCreationContextFactory.newCreatingContext(documentLocation);

        assertEquals("test-id", result.getId());
        assertEquals("test-source", result.getSource());
        assertEquals("http://test-url/", result.getUrl());
        assertEquals(5, result.getDownloaderVersion());
    }
}
