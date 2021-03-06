package com.github.loa.downloader.service;

import com.github.loa.downloader.configuration.DownloaderConfigurationProperties;
import com.github.loa.location.domain.DocumentLocation;
import com.github.loa.location.service.factory.domain.DocumentLocationCreationContext;
import com.github.loa.location.service.id.factory.DocumentLocationIdFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URL;

@Service
@RequiredArgsConstructor
public class DocumentLocationCreationContextFactory {

    private final DocumentLocationIdFactory documentLocationIdFactory;
    private final DownloaderConfigurationProperties downloaderConfigurationProperties;

    public DocumentLocationCreationContext newCreatingContext(final DocumentLocation documentSourceItem) {
        final URL documentLocation = documentSourceItem.getLocation().toUrl().orElseThrow();

        final String documentId = documentLocationIdFactory.newDocumentLocationId(documentLocation);

        return DocumentLocationCreationContext.builder()
                .id(documentId)
                .url(documentLocation.toString())
                .source(documentSourceItem.getSourceName())
                .downloaderVersion(downloaderConfigurationProperties.getVersionNumber())
                .build();
    }
}
