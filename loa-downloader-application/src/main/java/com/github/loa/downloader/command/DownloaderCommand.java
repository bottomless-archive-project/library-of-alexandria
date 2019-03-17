package com.github.loa.downloader.command;

import com.github.loa.downloader.command.service.DocumentDownloaderProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DownloaderCommand implements CommandLineRunner {

    private final DocumentDownloaderProcessor documentDownloaderProcessor;

    @Override
    public void run(String... args) {
        documentDownloaderProcessor.downloadDocuments();
    }
}
