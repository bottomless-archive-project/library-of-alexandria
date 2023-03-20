package com.github.bottomlessarchive.loa.downloader.service.source.beacon.loader;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "loa.downloader.source", havingValue = "beacon-loader")
public class BeaconDocumentLoader implements CommandLineRunner {

    private final MetricLogger metricLogger;
    private final ApplicationContext applicationContext;
    private final BeaconDocumentArchiver beaconDocumentArchiver;
    private final BeaconDownloaderConfigurationProperties beaconDownloaderConfigurationProperties;

    @Qualifier("downloaderExecutorService")
    private final ExecutorService downloaderExecutorService;

    @Qualifier("processedDocumentCount")
    private final Counter processedDocumentCount;

    @Qualifier("archivedDocumentCount")
    private final Counter archivedDocumentCount;

    @Override
    public void run(final String... args) throws IOException {
        final Path sourceFolder = beaconDownloaderConfigurationProperties.location();

        log.info("Started processing documents at folder: {}.", sourceFolder);

        try (Stream<Path> files = Files.list(sourceFolder)) {
            files.forEach(beaconDocumentArchiver::archiveFile);
        }

        awaitTasksToFinish();
        logFinalStatistics();
        shutdownApplication();

        log.info("Finished processing documents at folder: {}.", sourceFolder);
    }

    private void logFinalStatistics() {
        metricLogger.logCounters(archivedDocumentCount, processedDocumentCount);
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
