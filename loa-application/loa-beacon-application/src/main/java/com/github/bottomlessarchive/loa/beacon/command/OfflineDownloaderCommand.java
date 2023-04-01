package com.github.bottomlessarchive.loa.beacon.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bottomlessarchive.loa.beacon.command.configuration.OfflineDownloaderConfigurationProperties;
import com.github.bottomlessarchive.loa.beacon.service.DocumentLocationVisitor;
import com.github.bottomlessarchive.loa.beacon.service.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.loadoc.domain.LoadocMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.beacon.offline-enabled", havingValue = "true")
public class OfflineDownloaderCommand implements CommandLineRunner {

    private final ObjectMapper objectMapper;
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
                            throw new RuntimeException(e); //TODO: Do something with this!
                        }
                    })
                    .forEach(documentLocation ->
                            documentLocationVisitor.visitDocumentLocation(documentLocation, persistenceEntity -> {
                                try {
                                    byte[] loadocMetadataData = objectMapper.writeValueAsBytes(
                                            LoadocMetadata.builder()
                                                    .id(persistenceEntity.documentLocation().getId())
                                                    .type(persistenceEntity.documentLocation().getType().name())
                                                    .sourceName(persistenceEntity.documentLocation().getSourceName())
                                                    .documentLocationId(persistenceEntity.documentLocationResult().id())
                                                    .downloadResultCode(persistenceEntity.documentLocationResult().resultType().name())
                                                    .build()
                                    );

                                    try (OutputStream outputStream = Files.newOutputStream(persistenceEntity.storagePath())) {
                                        outputStream.write(loadocMetadataData.length);
                                        outputStream.write(loadocMetadataData);

                                        persistenceEntity.stageLocation().moveTo(outputStream);
                                    }
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                    );
        }
    }
}
