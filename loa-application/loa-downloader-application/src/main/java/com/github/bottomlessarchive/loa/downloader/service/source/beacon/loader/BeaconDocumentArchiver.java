package com.github.bottomlessarchive.loa.downloader.service.source.beacon.loader;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.downloader.service.document.DocumentArchiver;
import com.github.bottomlessarchive.loa.downloader.service.document.domain.DocumentArchivingContext;
import com.github.bottomlessarchive.loa.downloader.service.source.beacon.loader.domain.LoaderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeaconDocumentArchiver {

    private final DocumentArchiver documentArchiver;
    private final DocumentEntityFactory documentEntityFactory;

    public void archiveFile(final Path path) {
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

        deleteFile(path);
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
