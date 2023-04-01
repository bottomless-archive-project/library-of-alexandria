package com.github.bottomlessarchive.loa.downloader.service.source.beacon.loadocloader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bottomlessarchive.loa.downloader.service.document.DocumentArchiver;
import com.github.bottomlessarchive.loa.downloader.service.document.domain.DocumentArchivingContext;
import com.github.bottomlessarchive.loa.downloader.service.source.beacon.loader.domain.LoaderException;
import com.github.bottomlessarchive.loa.loadoc.domain.LoadocMetadata;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocationResultType;
import com.github.bottomlessarchive.loa.location.service.DocumentLocationManipulator;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeaconLoadocDocumentArchiver {

    private final ObjectMapper objectMapper;
    private final DocumentArchiver documentArchiver;
    private final DocumentLocationManipulator documentLocationManipulator;

    @SneakyThrows
    public void archiveLoadocFile(final Path path) {
        Path tempFileForContent = null;

        //TODO: Move this low level functionality to its own class
        try (DataInputStream inputStream = new DataInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            final int loadocMetadataDataLength = inputStream.readInt();
            final byte[] loadocMetadataData = new byte[loadocMetadataDataLength];
            inputStream.read(loadocMetadataData);

            final LoadocMetadata loadocMetadata = objectMapper.readValue(loadocMetadataData, LoadocMetadata.class);

            tempFileForContent = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");

            Files.copy(inputStream, tempFileForContent);

            documentLocationManipulator.updateDownloadResultCode(loadocMetadata.documentLocationId(), DocumentLocationResultType.valueOf(
                    loadocMetadata.downloadResultCode()));

            final DocumentArchivingContext documentArchivingContext = DocumentArchivingContext.builder()
                    .id(UUID.fromString(loadocMetadata.id()))
                    .fromBeacon(true)
                    .type(DocumentType.valueOf(loadocMetadata.type()))
                    .source(loadocMetadata.sourceName())
                    .contents(path)
                    .build();

            documentArchiver.archiveDocument(documentArchivingContext);

            deleteFile(path);
        } finally {
            if (tempFileForContent != null) {
                Files.deleteIfExists(tempFileForContent);
            }
        }
    }

    private void deleteFile(final Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            log.error("Failed to delete file at path: {}.", path, e);

            throw new LoaderException("Failed to delete file at path: " + path + ".", e);
        }
    }
}
