package com.github.bottomlessarchive.loa.downloader.service.source.beacon.loadocloader;

import com.github.bottomlessarchive.loa.downloader.service.source.beacon.loadocloader.configuration.LoadocLoaderBeaconConfigurationProperties;
import com.github.bottomlessarchive.loa.logging.service.MetricLogger;
import com.github.bottomlessarchive.loa.threading.executor.BlockingExecutor;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "loa.downloader.source", havingValue = "loadoc-loader")
public class BeaconLoadocDocumentLoader implements CommandLineRunner {

    private final MetricLogger metricLogger;
    private final ApplicationContext applicationContext;
    private final BeaconLoadocDocumentArchiver beaconLoadocDocumentArchiver;
    private final LoadocLoaderBeaconConfigurationProperties loadocLoaderBeaconConfigurationProperties;

    @Qualifier("downloaderExecutorService")
    private final BlockingExecutor downloaderExecutorService;

    @Qualifier("processedDocumentCount")
    private final Counter processedDocumentCount;

    @Qualifier("archivedDocumentCount")
    private final Counter archivedDocumentCount;

    @Override
    public void run(final String... args) throws Exception {
        final Path sourceFolder = loadocLoaderBeaconConfigurationProperties.location();

        log.info("Started processing documents at folder: {}.", sourceFolder);

        try (Stream<Path> files = Files.list(sourceFolder)) {
            files.forEach(beaconLoadocDocumentArchiver::archiveLoadocFile);
        }

        downloaderExecutorService.awaitTasksToFinish();

        logFinalStatistics();
        shutdownApplication();

        log.info("Finished processing documents at folder: {}.", sourceFolder);
    }

    private void logFinalStatistics() {
        metricLogger.logCounters(archivedDocumentCount, processedDocumentCount);
    }

    private void shutdownApplication() {
        ((ConfigurableApplicationContext) applicationContext).close();
    }
}
