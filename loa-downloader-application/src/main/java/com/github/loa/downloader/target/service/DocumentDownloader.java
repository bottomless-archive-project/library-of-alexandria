package com.github.loa.downloader.target.service;

import com.github.loa.downloader.document.service.DocumentEntityFactory;
import com.github.loa.downloader.document.service.DocumentIdFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

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
    private final StagingLocationFactory stagingLocationFactory;

    public void downloadDocument(final URL documentLocation) {
        final String documentId = documentIdFactory.newDocumentId(documentLocation);

        if (!shouldDownload(documentId)) {
            log.debug("Document location already visited: " + documentLocation);

            return;
        }

        final File temporaryFile = stagingLocationFactory.newStagingLocation(documentId);

        fileDownloader.downloadFile(documentLocation, temporaryFile, 30000);

        final long fileSize = temporaryFile.length();
        final String crc = calculateHash(temporaryFile);

        //TODO check uniqueness
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
