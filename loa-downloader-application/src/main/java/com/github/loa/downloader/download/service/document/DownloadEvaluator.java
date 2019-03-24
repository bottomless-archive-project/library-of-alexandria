package com.github.loa.downloader.download.service.document;

import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.loa.downloader.command.configuration.DownloaderConfiguration;
import com.github.loa.source.configuration.DocumentSourceConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadEvaluator {

    private final DocumentEntityFactory documentEntityFactory;
    private final DocumentSourceConfiguration documentSourceConfiguration;
    private final DownloaderConfiguration downloaderConfiguration;

    //@Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean evaluateDocument(final String documentId, final URL documentLocation) {
        if (!shouldDownload(documentId)) {
            log.debug("Document location already visited: {}.", documentLocation);

            return false;
        }

        documentEntityFactory.newDocumentEntity(
                DocumentCreationContext.builder()
                        .id(documentId)
                        .location(documentLocation)
                        .status(DocumentStatus.UNDER_CRAWL)
                        .versionNumber(downloaderConfiguration.getVersionNumber())
                        .source(documentSourceConfiguration.getName())
                        .build()
        );

        return true;
    }

    private boolean shouldDownload(final String documentId) {
        return !documentEntityFactory.isDocumentExists(documentId);
    }
}
