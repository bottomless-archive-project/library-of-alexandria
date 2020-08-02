package com.github.loa.downloader.service;

import com.github.loa.downloader.configuration.DownloaderConfigurationProperties;
import com.github.loa.location.domain.DocumentLocation;
import com.github.loa.location.service.factory.domain.DocumentLocationCreationContext;
import com.github.loa.location.service.id.factory.DocumentLocationIdFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentLocationCreationContextFactory {

    private final DocumentLocationIdFactory documentLocationIdFactory;
    private final DownloaderConfigurationProperties downloaderConfigurationProperties;

    public DocumentLocationCreationContext newCreatingContext(final DocumentLocation documentSourceItem) {
        final String documentId = documentLocationIdFactory
                .newDocumentLocationId(documentSourceItem.getLocation());

        return DocumentLocationCreationContext.builder()
                .id(documentId)
                .url(documentSourceItem.getLocation().toString())
                .source(documentSourceItem.getSourceName())
                .downloaderVersion(downloaderConfigurationProperties.getVersionNumber())
                .build();
    }
}
