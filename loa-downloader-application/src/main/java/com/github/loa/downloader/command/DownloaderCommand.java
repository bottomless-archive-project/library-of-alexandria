package com.github.loa.downloader.command;

import com.github.loa.downloader.source.service.DocumentSourceProvider;
import com.github.loa.downloader.target.service.DocumentDownloader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Slf4j
@Component
@RequiredArgsConstructor
public class DownloaderCommand implements CommandLineRunner {

    private final DocumentSourceProvider documentSourceProvider;
    private final DocumentDownloader documentDownloader;

    //TODO: Get these from properties
    private final ExecutorService executorService = Executors.newFixedThreadPool(100);
    private final Semaphore semaphore = new Semaphore(1000);

    @Override
    public void run(String... args) {
        documentSourceProvider.stream()
                //TODO: We should support other file types than pdf as well!
                //Using getPath() to be able to crawl urls like: /example/examplefile.pdf?queryparam=value
                .filter(url -> url.getPath().endsWith(".pdf"))
                .forEach(documentLocation -> {
                    try {
                        semaphore.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    executorService.submit(() -> {
                        documentDownloader.downloadDocument(documentLocation);

                        semaphore.release();
                    });
                });
    }
}
