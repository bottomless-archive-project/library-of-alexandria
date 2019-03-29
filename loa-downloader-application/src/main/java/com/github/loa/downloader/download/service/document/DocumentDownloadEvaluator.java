package com.github.loa.downloader.download.service.document;

import com.github.loa.document.service.id.factory.DocumentIdFactory;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.loa.downloader.command.configuration.DownloaderConfigurationProperties;
import com.github.loa.source.configuration.DocumentSourceConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;

/**
 * This service is responsible for evaluating if a document's download is necessary.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentDownloadEvaluator {

    private final DocumentEntityFactory documentEntityFactory;
    private final DocumentSourceConfiguration documentSourceConfiguration;
    private final DownloaderConfigurationProperties downloaderConfigurationProperties;
    private final DocumentIdFactory documentIdFactory;

    /**
     * Evaluates if a download of a document on the provided location is necessary. If it is then it will be added to
     * the database with the status of {@link DocumentStatus#UNDER_CRAWL}.
     *
     * @param documentLocation the location of the document
     * @return true if the document should be downloaded and processed
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean evaluateDocumentLocation(final URL documentLocation) {
        final String documentId = documentIdFactory.newDocumentId(documentLocation);

        if (!shouldDownload(documentId)) {
            log.debug("Document location already visited: {}.", documentLocation);

            return false;
        }

        documentEntityFactory.newDocumentEntity(
                DocumentCreationContext.builder()
                        .id(documentId)
                        .location(documentLocation)
                        .status(DocumentStatus.UNDER_CRAWL)
                        .versionNumber(downloaderConfigurationProperties.getVersionNumber())
                        .source(documentSourceConfiguration.getName())
                        .build()
        );

        return true;
    }

    private boolean shouldDownload(final String documentId) {
        return !documentEntityFactory.isDocumentExists(documentId);
    }
}
