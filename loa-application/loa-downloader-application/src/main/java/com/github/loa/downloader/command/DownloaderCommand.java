package com.github.loa.downloader.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DownloaderCommand implements CommandLineRunner {

    @Override
    public void run(final String... args) {
        log.info("Initializing document processing.");

        //TODO: Coming from a queue
                /*.flatMap(documentDownloader::downloadDocument)
                .subscribe();*/
    }
}
