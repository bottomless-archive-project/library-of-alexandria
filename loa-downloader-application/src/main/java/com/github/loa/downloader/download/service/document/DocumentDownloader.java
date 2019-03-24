package com.github.loa.downloader.download.service.document;

import com.github.loa.document.service.DocumentEntityManipulator;
import com.github.loa.document.service.DocumentIdFactory;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.downloader.download.service.file.FileDownloader;
import com.github.loa.downloader.download.service.file.domain.FileDownloaderException;
import com.github.loa.stage.service.StageLocationFactory;
import com.github.loa.vault.service.VaultLocationFactory;
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
    private final StageLocationFactory stageLocationFactory;
    private final VaultLocationFactory vaultLocationFactory;
    private final DownloadEvaluator downloadEvaluator;
    private final DocumentEntityManipulator documentEntityManipulator;

    public void downloadDocument(final URL documentLocation) {
        log.debug("Starting to download document {}.", documentLocation);

        final String documentId = documentIdFactory.newDocumentId(documentLocation);

        if (!downloadEvaluator.evaluateDocument(documentId, documentLocation)) {
            return;
        }

        final File temporaryFile = stageLocationFactory.newLocation(documentId);

        try {
            fileDownloader.downloadFile(documentLocation, temporaryFile, 30000);
        } catch (FileDownloaderException e) {
            log.info("Failed to download document!", e);

            documentEntityManipulator.updateStatus(documentId, DocumentStatus.FAILED);
        }

        //TODO: If filesize is 0 mark it as failed not duplicate!
        final String crc = calculateHash(temporaryFile);
        final long fileSize = temporaryFile.length();

        // Validate if the file already exists or not
        if (documentEntityFactory.isDocumentExists(crc, fileSize)) {
            temporaryFile.delete();

            documentEntityManipulator.updateFileSizeAndCrc(documentId, fileSize, crc);
            documentEntityManipulator.updateStatus(documentId, DocumentStatus.DUPLICATE);

            return;
        }

        documentEntityManipulator.updateFileSizeAndCrc(documentId, fileSize, crc);

        // The file is not a valid pdf
        if (fileSize <= 1024) {
            temporaryFile.delete();

            documentEntityManipulator.updateStatus(documentId, DocumentStatus.INVALID);

            return;
        }

        try {
            Files.move(temporaryFile.toPath(), vaultLocationFactory.newLocation(documentId).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed while processing the downloaded document.", e);

            documentEntityManipulator.updateStatus(documentId, DocumentStatus.PROCESSING_FAILURE);

            return;
        }

        documentEntityManipulator.updateStatus(documentId, DocumentStatus.DOWNLOADED);
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
