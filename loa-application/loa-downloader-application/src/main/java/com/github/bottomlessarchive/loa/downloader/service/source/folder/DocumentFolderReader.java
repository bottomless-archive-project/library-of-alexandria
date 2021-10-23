package com.github.bottomlessarchive.loa.downloader.service.source.folder;

import com.github.bottomlessarchive.loa.downloader.service.source.configuration.DownloaderFolderSourceConfiguration;
import com.github.bottomlessarchive.loa.location.domain.link.UrlLink;
import com.github.bottomlessarchive.loa.downloader.service.document.DocumentLocationProcessor;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "loa.downloader.source", havingValue = "folder")
public class DocumentFolderReader implements CommandLineRunner {

    private final DownloaderFolderSourceConfiguration downloaderFolderSourceConfiguration;
    private final DocumentLocationProcessor documentLocationProcessor;

    @Override
    public void run(final String... args) {
        final File sourceFolder = new File(downloaderFolderSourceConfiguration.getLocation());

        //TODO: Why not Flux.generate like DownloadQueueListener?
        Flux.fromArray(Objects.requireNonNull(sourceFolder.listFiles()))
                .map(this::buildDocumentSourceItem)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(documentLocationProcessor::processDocumentLocation)
                .subscribe();
    }

    @SneakyThrows
    private DocumentLocation buildDocumentSourceItem(final File file) {
        return DocumentLocation.builder()
                .location(
                        UrlLink.builder()
                                .url(file.toURI().toURL())
                                .build()
                )
                .build();
    }
}
