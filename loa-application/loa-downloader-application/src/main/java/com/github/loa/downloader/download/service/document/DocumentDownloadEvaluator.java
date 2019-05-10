package com.github.loa.downloader.download.service.document;

import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.location.factory.DocumentLocationEntityFactory;
import com.github.loa.document.service.location.id.factory.DocumentLocationIdFactory;
import com.github.loa.downloader.command.configuration.DownloaderConfigurationProperties;
import com.github.loa.source.configuration.DocumentSourceConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;

/**
 * This service is responsible for evaluating if a document's download is necessary.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentDownloadEvaluator {

    private final DocumentSourceConfiguration documentSourceConfiguration;
    private final DownloaderConfigurationProperties downloaderConfigurationProperties;
    private final DocumentLocationIdFactory documentLocationIdFactory;
    private final DocumentLocationEntityFactory documentLocationEntityFactory;

    /**
     * Evaluates if a download of a document on the provided location is necessary. If it is then it will be added to
     * the database with the status of {@link DocumentStatus#UNDER_CRAWL}.
     *
     * @param documentLocation the location of the document
     * @return true if the document should be downloaded and processed
     */
    public boolean evaluateDocumentLocation(final URL documentLocation) {
        final String documentLocationId = documentLocationIdFactory.newDocumentId(documentLocation);

        if (!shouldDownload(documentLocationId)) {
            log.debug("Document location already visited: {}.", documentLocation);

            return false;
        }

        documentLocationEntityFactory.newDocumentLocationEntity(documentLocationId, documentLocation,
                downloaderConfigurationProperties.getVersionNumber(), documentSourceConfiguration.getName());

        return true;
    }

    private boolean shouldDownload(final String documentId) {
        return !documentLocationEntityFactory.isDocumentLocationExists(documentId);
    }
}
