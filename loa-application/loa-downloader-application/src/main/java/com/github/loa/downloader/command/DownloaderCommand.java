package com.github.loa.downloader.command;

import com.github.loa.document.service.location.DocumentLocationValidator;
import com.github.loa.downloader.command.batch.DocumentLocationFactory;
import com.github.loa.downloader.download.service.document.DocumentDownloader;
import com.github.loa.url.service.UrlEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DownloaderCommand implements CommandLineRunner {

    private final DocumentLocationFactory documentLocationFactory;
    private final DocumentLocationValidator documentLocationValidator;
    private final DocumentDownloader documentDownloader;
    private final UrlEncoder urlEncoder;

    @Override
    public void run(final String... args) {
        log.info("Initializing document processing.");

        documentLocationFactory.streamLocations()
                .filter(documentLocationValidator::validDocumentLocation)
                .flatMap(urlEncoder::encode)
                .distinct()
                .flatMap(documentDownloader::downloadDocument)
                .subscribe();
    }
}
