package com.github.loa.downloader.command;

import com.github.loa.downloader.command.service.DocumentSourceProcessor;
import com.github.loa.source.service.DocumentSourceProviderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DownloaderCommand implements CommandLineRunner {

    private final DocumentSourceProcessor documentSourceProcessor;
    private final DocumentSourceProviderFactory documentSourceProviderFactory;

    @Override
    public void run(final String... args) {
        log.info("Initializing document processing.");

        documentSourceProcessor.processDocumentSource(documentSourceProviderFactory.openSource());
    }
}
