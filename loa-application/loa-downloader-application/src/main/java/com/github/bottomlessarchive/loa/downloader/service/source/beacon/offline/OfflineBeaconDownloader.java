package com.github.bottomlessarchive.loa.downloader.service.source.beacon.offline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bottomlessarchive.loa.downloader.service.source.beacon.offline.configuration.OfflineBeaconDownloaderConfigurationProperties;
import com.github.bottomlessarchive.loa.downloader.service.source.beacon.service.DocumentLocationCollector;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "loa.downloader.source", havingValue = "beacon-offline")
public class OfflineBeaconDownloader implements CommandLineRunner {

    private final ObjectMapper objectMapper;
    private final QueueManipulator queueManipulator;
    private final DocumentLocationCollector documentLocationCollector;
    private final OfflineBeaconDownloaderConfigurationProperties offlineBeaconDownloaderConfigurationProperties;

    @Override
    public void run(String... args) throws IOException {
        queueManipulator.silentlyInitializeQueues(Queue.DOCUMENT_LOCATION_QUEUE, Queue.DOCUMENT_ARCHIVING_QUEUE);

        final List<DocumentLocation> documentLocationMessages = documentLocationCollector.collectDocumentsToProcess(
                offlineBeaconDownloaderConfigurationProperties.resultSize());

        try (OutputStream outputStream = Files.newOutputStream(offlineBeaconDownloaderConfigurationProperties.resultLocation())) {
            documentLocationMessages.forEach(location -> writeLocation(location, outputStream));
        }
    }

    @SneakyThrows
    private void writeLocation(final DocumentLocation documentLocation, final OutputStream outputStream) {
        outputStream.write(objectMapper.writeValueAsBytes(documentLocation));
        outputStream.write((byte) '\n');
    }
}
