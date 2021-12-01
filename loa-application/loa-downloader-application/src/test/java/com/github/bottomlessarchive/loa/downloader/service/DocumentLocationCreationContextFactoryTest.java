package com.github.bottomlessarchive.loa.downloader.service;

import com.github.bottomlessarchive.loa.downloader.configuration.DownloaderConfigurationProperties;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.location.domain.link.StringLink;
import com.github.bottomlessarchive.loa.location.domain.link.UrlLink;
import com.github.bottomlessarchive.loa.location.service.factory.domain.DocumentLocationCreationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
                .location(StringLink.builder()
                        .link("http://test-url/")
                        .build()
                )
                .build();
        when(downloaderConfigurationProperties.getVersionNumber())
                .thenReturn(5);

        final DocumentLocationCreationContext result = documentLocationCreationContextFactory.newCreatingContext(documentLocation);

        assertEquals("test-id", result.getId());
        assertEquals("test-source", result.getSource());
        assertEquals("http://test-url/", result.getUrl());
        assertEquals(5, result.getDownloaderVersion());
    }

    @Test
    void testWhenDocumentLocationIsMissing() {
        final DocumentLocation documentLocation = DocumentLocation.builder()
                .location(UrlLink.builder().build())
                .build();

        assertThrows(IllegalStateException.class, () -> documentLocationCreationContextFactory.newCreatingContext(documentLocation));
    }
}
