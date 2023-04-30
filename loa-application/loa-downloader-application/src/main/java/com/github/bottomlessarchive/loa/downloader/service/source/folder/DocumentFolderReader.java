package com.github.bottomlessarchive.loa.downloader.service.source.folder;

import com.github.bottomlessarchive.loa.downloader.service.document.DocumentLocationProcessorWrapper;
import com.github.bottomlessarchive.loa.downloader.service.source.configuration.DownloaderFolderSourceConfigurationProperties;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.logging.service.MetricLogger;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
@ConditionalOnProperty(value = "loa.downloader.source", havingValue = "folder")
public class DocumentFolderReader implements CommandLineRunner {

    private final DownloaderFolderSourceConfigurationProperties downloaderFolderSourceConfigurationProperties;
    private final DocumentLocationProcessorWrapper documentLocationProcessorWrapper;
    @Qualifier("downloaderExecutorService")
    private final ExecutorService downloaderExecutorService;
    private final ApplicationContext applicationContext;

    private final MetricLogger metricLogger;

    @Qualifier("processedDocumentCount")
    private final Counter processedDocumentCount;

    @Qualifier("archivedDocumentCount")
    private final Counter archivedDocumentCount;

    @Override
    @SneakyThrows
    public void run(final String... args) {
        final Path sourceFolder = Path.of(downloaderFolderSourceConfigurationProperties.location());

        log.info("Started processing documents at folder: {}.", sourceFolder);

        try (Stream<Path> files = Files.list(sourceFolder)) {
            files.forEach(path -> documentLocationProcessorWrapper.processDocumentLocation(buildDocumentSourceItem(path), () -> {
                                if (downloaderFolderSourceConfigurationProperties.shouldRemove()) {
                                    try {
                                        log.debug("Deleting file at: {}.", path);

                                        Files.deleteIfExists(path);
                                    } catch (final IOException e) {
                                        log.error("Failed to delete source file!", e);
                                    }
                                }
                            }
                    )
            );
        }

        awaitTasksToFinish();
        logFinalStatistics();
        shutdownApplication();
    }

    @SneakyThrows
    private DocumentLocation buildDocumentSourceItem(final Path file) {
        log.debug("Starting to parse document at location: {}.", file);

        return DocumentLocation.builder()
                .location(file.toUri().toString())
                .sourceName(downloaderFolderSourceConfigurationProperties.sourceName())
                .build();
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
