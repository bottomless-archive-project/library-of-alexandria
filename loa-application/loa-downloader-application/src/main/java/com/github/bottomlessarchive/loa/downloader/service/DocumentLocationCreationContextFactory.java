package com.github.bottomlessarchive.loa.downloader.service;

import com.github.bottomlessarchive.loa.downloader.configuration.DownloaderConfigurationProperties;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.location.service.factory.domain.DocumentLocationCreationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentLocationCreationContextFactory {

    private final DownloaderConfigurationProperties downloaderConfigurationProperties;

    public DocumentLocationCreationContext newCreatingContext(final DocumentLocation documentLocation) {
        return DocumentLocationCreationContext.builder()
                .id(documentLocation.getId())
                .url(documentLocation.getLocation())
                .source(documentLocation.getSourceName())
                .downloaderVersion(downloaderConfigurationProperties.versionNumber())
                .build();
    }
}
