package com.github.loa.downloader.service;

import com.github.loa.downloader.configuration.DownloaderConfigurationProperties;
import com.github.loa.location.service.factory.domain.DocumentLocationCreationContext;
import com.github.loa.location.service.id.factory.DocumentLocationIdFactory;
import com.github.loa.source.domain.DocumentSourceItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentLocationCreationContextFactory {

    private final DocumentLocationIdFactory documentLocationIdFactory;
    private final DownloaderConfigurationProperties downloaderConfigurationProperties;

    public DocumentLocationCreationContext newCreatingContext(final DocumentSourceItem documentSourceItem) {
        final String documentId = documentLocationIdFactory
                .newDocumentLocationId(documentSourceItem.getDocumentLocation());

        return DocumentLocationCreationContext.builder()
                .id(documentId)
                .url(documentSourceItem.getDocumentLocation().toString())
                .source(documentSourceItem.getSourceName())
                .downloaderVersion(downloaderConfigurationProperties.getVersionNumber())
                .build();
    }
}
