package com.github.loa.downloader.service.source.folder;

import com.github.loa.downloader.configuration.DownloaderConfigurationProperties;
import com.github.loa.downloader.service.document.DocumentLocationProcessor;
import com.github.loa.source.domain.DocumentSourceItem;
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

    private final DownloaderConfigurationProperties downloaderConfigurationProperties;
    private final DocumentLocationProcessor documentLocationProcessor;

    @Override
    public void run(final String... args) {
        final File sourceFolder = new File(downloaderConfigurationProperties.getFolderSourceLocation());

        //TODO: Why not Flux.generate like DownloadQueueListener?
        Flux.fromArray(Objects.requireNonNull(sourceFolder.listFiles()))
                .map(this::buildDocumentSourceItem)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(documentLocationProcessor::processDocumentLocation)
                .subscribe();
    }

    @SneakyThrows
    private DocumentSourceItem buildDocumentSourceItem(final File file) {
        return DocumentSourceItem.builder()
                .documentLocation(file.toURI().toURL())
                .build();
    }
}
