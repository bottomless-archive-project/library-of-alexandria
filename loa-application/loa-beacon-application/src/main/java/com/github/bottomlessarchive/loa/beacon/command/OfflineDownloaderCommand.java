package com.github.bottomlessarchive.loa.beacon.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bottomlessarchive.loa.beacon.command.configuration.OfflineDownloaderConfigurationProperties;
import com.github.bottomlessarchive.loa.beacon.service.DocumentLocationVisitor;
import com.github.bottomlessarchive.loa.beacon.service.StoragePathFactory;
import com.github.bottomlessarchive.loa.beacon.service.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.loadoc.domain.LoadocMetadata;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.beacon.offline.enabled", havingValue = "true")
public class OfflineDownloaderCommand implements CommandLineRunner {

    private final ObjectMapper objectMapper;
    private final Counter processedDocumentCount;
    private final StoragePathFactory storagePathFactory;
    private final DocumentLocationVisitor documentLocationVisitor;
    private final OfflineDownloaderConfigurationProperties offlineDownloaderConfigurationProperties;

    @Override
    public void run(final String... args) throws IOException {
        try (Stream<String> sourceUrls = Files.lines(offlineDownloaderConfigurationProperties.sourceFile())) {
            sourceUrls
                    .skip(offlineDownloaderConfigurationProperties.skip())
                    .map(line -> {
                        try {
                            return objectMapper.readValue(line, DocumentLocation.class);
                        } catch (JsonProcessingException e) {
                            throw new IllegalStateException("Failed to parse document location!", e);
                        }
                    })
                    .forEach(documentLocation ->
                            documentLocationVisitor.visitDocumentLocation(documentLocation,
                                    persistenceEntity -> {
                                        try {
                                            final byte[] loadocMetadataData = objectMapper.writeValueAsBytes(
                                                    LoadocMetadata.builder()
                                                            .id(persistenceEntity.documentLocation().getId())
                                                            .type(persistenceEntity.documentLocation().getType().name())
                                                            .sourceName(persistenceEntity.documentLocation().getSourceName())
                                                            .documentId(persistenceEntity.documentLocationResult().id())
                                                            .downloadResultCode(persistenceEntity.documentLocationResult()
                                                                    .resultType().name())
                                                            .build()
                                            );

                                            try (OutputStream outputStream = Files.newOutputStream(persistenceEntity.storagePath())) {
                                                outputStream.write(loadocMetadataData.length);
                                                outputStream.write(loadocMetadataData);

                                                try (StageLocation stageLocation = persistenceEntity.stageLocation()) {
                                                    stageLocation.moveTo(outputStream);
                                                }
                                            }
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        } finally {
                                            processedDocumentCount.increment();
                                        }
                                    },
                                    failureEntity -> {
                                        try {
                                            final UUID documentId = UUID.randomUUID();

                                            final byte[] loadocMetadataData = objectMapper.writeValueAsBytes(
                                                    LoadocMetadata.builder()
                                                            .id(failureEntity.documentLocation().getId())
                                                            .type(failureEntity.documentLocation().getType().name())
                                                            .sourceName(failureEntity.documentLocation().getSourceName())
                                                            .documentId(documentId.toString())
                                                            .downloadResultCode(failureEntity.downloadResult().name())
                                                            .build()
                                            );

                                            final Path storagePath = storagePathFactory.buildStoragePath(documentId);

                                            try (OutputStream outputStream = Files.newOutputStream(storagePath)) {
                                                outputStream.write(loadocMetadataData.length);
                                                outputStream.write(loadocMetadataData);
                                            }
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        } finally {
                                            processedDocumentCount.increment();
                                        }
                                    })
                    );
        }
    }
}
