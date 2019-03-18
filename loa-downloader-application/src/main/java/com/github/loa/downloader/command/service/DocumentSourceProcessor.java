package com.github.loa.downloader.command.service;

import com.github.loa.downloader.source.service.DocumentSourceProvider;
import com.github.loa.downloader.target.service.DocumentDownloader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

/**
 * This service is the hearth of the downloader application. It's responsible for the processing
 * (loading and downloading) of document locations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentSourceProcessor {

    private final DocumentDownloader documentDownloader;
    private final ExecutorService downloaderExecutor;
    private final Semaphore downloaderSemaphore;

    /**
     * Handles the processing of a document source.
     *
     * @param documentSourceProvider provides the source to process
     */
    public void processDocumentSource(final DocumentSourceProvider documentSourceProvider) {
        documentSourceProvider.stream()
                .filter(this::shouldDownload)
                .forEach(this::processLocation);
    }

    private boolean shouldDownload(final URL documentLocation) {
        //TODO: We should support other file types than pdf as well!
        //Using getPath() to be able to crawl urls like: /example/examplefile.pdf?queryparam=value
        return documentLocation.getPath().endsWith(".pdf");
    }

    private void processLocation(final URL documentLocation) {
        try {
            downloaderSemaphore.acquire();
        } catch (InterruptedException e) {
            log.error("Unable to acquire space in the download queue!", e);
        }

        downloaderExecutor.submit(() -> {
            documentDownloader.downloadDocument(documentLocation);

            downloaderSemaphore.release();
        });
    }
}
