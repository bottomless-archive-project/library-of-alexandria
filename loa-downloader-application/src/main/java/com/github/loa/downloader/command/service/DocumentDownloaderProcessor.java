package com.github.loa.downloader.command.service;

import com.github.loa.downloader.source.service.DocumentSourceProvider;
import com.github.loa.downloader.target.service.DocumentDownloader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

//TODO: Rename this to something more meaningful!
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentDownloaderProcessor {

    private final DocumentSourceProvider documentSourceProvider;
    private final DocumentDownloader documentDownloader;
    private final ExecutorService downloaderExecutor;
    private final Semaphore downloaderSemaphore;

    public void downloadDocuments() {
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
