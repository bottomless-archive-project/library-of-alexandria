package com.github.loa.downloader.target.service.document;

import com.github.loa.document.service.DocumentIdFactory;
import com.github.loa.downloader.command.configuration.DownloaderConfiguration;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.downloader.source.configuration.DocumentSourceConfiguration;
import com.github.loa.downloader.target.service.file.FileDownloader;
import com.github.loa.downloader.target.service.file.domain.FileDownloaderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * This service is responsible for downloading documents.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentDownloader {

    private final DocumentEntityFactory documentEntityFactory;
    private final FileDownloader fileDownloader;
    private final DocumentIdFactory documentIdFactory;
    private final DocumentStagingLocationFactory documentStagingLocationFactory;
    private final DocumentLocationFactory documentLocationFactory;
    private final DocumentSourceConfiguration documentSourceConfiguration;
    private final DownloaderConfiguration downloaderConfiguration;

    public void downloadDocument(final URL documentLocation) {
        log.debug("Starting to download document {}.", documentLocation);

        final String documentId = documentIdFactory.newDocumentId(documentLocation);

        if (!shouldDownload(documentId)) {
            log.debug("Document location already visited: {}.", documentLocation);

            return;
        }

        final File temporaryFile = documentStagingLocationFactory.newStagingLocation(documentId);

        try {
            fileDownloader.downloadFile(documentLocation, temporaryFile, 30000);
        } catch (FileDownloaderException e) {

            documentEntityFactory.newDocumentEntity(
                    DocumentCreationContext.builder()
                            .id(documentId)
                            .location(documentLocation)
                            .status(DocumentStatus.FAILED)
                            .versionNumber(downloaderConfiguration.getVersionNumber())
                            .source(documentSourceConfiguration.getName())
                            .build()
            );
        }

        final String crc = calculateHash(temporaryFile);
        final long fileSize = temporaryFile.length();

        // Validate if the file already exists or not
        if (documentEntityFactory.isDocumentExists(crc, fileSize)) {
            temporaryFile.delete();

            documentEntityFactory.newDocumentEntity(
                    DocumentCreationContext.builder()
                            .id(documentId)
                            .location(documentLocation)
                            .crc(crc)
                            .fileSize(fileSize)
                            .status(DocumentStatus.DUPLICATE)
                            .versionNumber(downloaderConfiguration.getVersionNumber())
                            .source(documentSourceConfiguration.getName())
                            .build()
            );

            return;
        }

        // The file is not a valid pdf
        if (fileSize <= 1024) {
            temporaryFile.delete();

            documentEntityFactory.newDocumentEntity(
                    DocumentCreationContext.builder()
                            .id(documentId)
                            .location(documentLocation)
                            .crc(crc)
                            .fileSize(fileSize)
                            .status(DocumentStatus.INVALID)
                            .versionNumber(downloaderConfiguration.getVersionNumber())
                            .source(documentSourceConfiguration.getName())
                            .build()
            );

            return;
        }

        try {
            Files.move(temporaryFile.toPath(), documentLocationFactory.newLocation(documentId).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            //TODO: Handle this better!
            e.printStackTrace();
        }

        documentEntityFactory.newDocumentEntity(
                DocumentCreationContext.builder()
                        .id(documentId)
                        .location(documentLocation)
                        .crc(crc)
                        .fileSize(fileSize)
                        .status(DocumentStatus.DOWNLOADED)
                        .versionNumber(downloaderConfiguration.getVersionNumber())
                        .source(documentSourceConfiguration.getName())
                        .build()
        );
    }

    private boolean shouldDownload(final String documentId) {
        return !documentEntityFactory.isDocumentExists(documentId);
    }

    private String calculateHash(final File documentDownloadingLocation) {
        try {
            try (final BufferedInputStream documentInputStream =
                         new BufferedInputStream(new FileInputStream(documentDownloadingLocation))) {
                return DigestUtils.sha256Hex(documentInputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to calculate file hash!", e);
        }
    }
}
