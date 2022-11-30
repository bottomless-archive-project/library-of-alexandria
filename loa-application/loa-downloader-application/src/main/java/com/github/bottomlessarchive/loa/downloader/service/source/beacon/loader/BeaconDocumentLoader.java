package com.github.bottomlessarchive.loa.downloader.service.source.beacon.loader;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.downloader.service.document.DocumentArchiver;
import com.github.bottomlessarchive.loa.downloader.service.document.domain.DocumentArchivingContext;
import com.github.bottomlessarchive.loa.downloader.service.source.beacon.configuration.BeaconDownloaderConfigurationProperties;
import com.github.bottomlessarchive.loa.logging.service.MetricLogger;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "loa.downloader.source", havingValue = "beacon-loader")
public class BeaconDocumentLoader implements CommandLineRunner {

    private final MetricLogger metricLogger;
    private final DocumentArchiver documentArchiver;
    private final ApplicationContext applicationContext;
    private final DocumentEntityFactory documentEntityFactory;
    private final BeaconDownloaderConfigurationProperties beaconDownloaderConfigurationProperties;

    @Qualifier("downloaderExecutorService")
    private final ExecutorService downloaderExecutorService;

    @Qualifier("processedDocumentCount")
    private final Counter processedDocumentCount;

    @Qualifier("archivedDocumentCount")
    private final Counter archivedDocumentCount;

    @Override
    public void run(String... args) throws IOException {
        final Path sourceFolder = Path.of(beaconDownloaderConfigurationProperties.location());

        log.info("Started processing documents at folder: {}.", sourceFolder);

        try (Stream<Path> files = Files.list(sourceFolder)) {
            files.forEach(path -> {
                final String fileName = path.getFileName().toString().split("\\.")[0];

                final DocumentEntity documentEntity = documentEntityFactory.getDocumentEntity(UUID.fromString(fileName))
                        .orElseThrow();

                final DocumentArchivingContext documentArchivingContext = DocumentArchivingContext.builder()
                        .id(UUID.fromString(fileName))
                        .fromBeacon(true)
                        .type(documentEntity.getType())
                        .source(documentEntity.getSource())
                        .contents(path)
                        .build();

                documentArchiver.archiveDocument(documentArchivingContext);

                try {
                    Files.delete(path);
                } catch (IOException e) {
                    //TODO: something better
                    throw new RuntimeException(e);
                }
            });
        }

        awaitTasksToFinish();
        logFinalStatistics();
        shutdownApplication();
    }

    private void logFinalStatistics() {
        metricLogger.logCounters(archivedDocumentCount, processedDocumentCount);

        log.info("Finished processing the folder.");
    }

    private void awaitTasksToFinish() {
        downloaderExecutorService.shutdown();
        try {
            if (!downloaderExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)) {
                downloaderExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            downloaderExecutorService.shutdownNow();
        }
    }

    private void shutdownApplication() {
        ((ConfigurableApplicationContext) applicationContext).close();
    }
}
