package com.github.bottomlessarchive.loa.downloader.service.source.beacon.loadocloader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bottomlessarchive.loa.checksum.service.ChecksumProvider;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
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
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeaconLoadocDocumentArchiver {

    private final ObjectMapper objectMapper;
    private final DocumentArchiver documentArchiver;
    private final DocumentEntityFactory documentEntityFactory;
    private final ChecksumProvider checksumProvider;
    private final DocumentLocationManipulator documentLocationManipulator;

    @SneakyThrows
    public void archiveLoadocFile(final Path path) {
        log.info("Archiving loadoc document at: {}.", path);

        Path tempFileForContent = null;

        //TODO: Move this low level functionality to its own class
        try (DataInputStream inputStream = new DataInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            final int loadocMetadataDataLength = inputStream.read();
            final byte[] loadocMetadataData = new byte[loadocMetadataDataLength];
            inputStream.read(loadocMetadataData);

            final LoadocMetadata loadocMetadata = objectMapper.readValue(loadocMetadataData, LoadocMetadata.class);

            tempFileForContent = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");

            Files.copy(inputStream, tempFileForContent, StandardCopyOption.REPLACE_EXISTING);

            documentLocationManipulator.updateDownloadResultCode(loadocMetadata.id(), DocumentLocationResultType.valueOf(
                    loadocMetadata.downloadResultCode()));

            if (!loadocMetadata.downloadResultCode().equals("OK")) {
                return;
            }

            final UUID documentId = UUID.fromString(loadocMetadata.documentId());

            final Optional<DocumentEntity> documentEntityOptional = documentEntityFactory.getDocumentEntity(
                    checksumProvider.checksum(Files.newInputStream(tempFileForContent)), Files.size(tempFileForContent),
                    loadocMetadata.type());

            if (documentEntityOptional.isPresent()) {
                log.info("Document with id: {} is a duplicate.", documentId);

                documentEntityFactory.addSourceLocation(documentEntityOptional.get().getId(), loadocMetadata.id());

                return;
            }

            final DocumentArchivingContext documentArchivingContext = DocumentArchivingContext.builder()
                    .id(documentId)
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
