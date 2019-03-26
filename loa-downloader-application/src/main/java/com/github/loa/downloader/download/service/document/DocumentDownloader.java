package com.github.loa.downloader.download.service.document;

import com.github.loa.checksum.service.ChecksumProvider;
import com.github.loa.document.service.id.factory.DocumentIdFactory;
import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.downloader.download.service.file.DocumentFileManipulator;
import com.github.loa.downloader.download.service.file.DocumentFileValidator;
import com.github.loa.downloader.download.service.file.FileDownloader;
import com.github.loa.downloader.download.service.file.domain.FileDownloaderException;
import com.github.loa.downloader.download.service.file.domain.FileManipulatingException;
import com.github.loa.stage.service.StageLocationFactory;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
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
    private final StageLocationFactory stageLocationFactory;
    private final DocumentFileValidator documentFileValidator;
    private final DocumentDownloadEvaluator documentDownloadEvaluator;
    private final DocumentManipulator documentManipulator;
    private final DocumentFileManipulator documentFileManipulator;
    private final ChecksumProvider checksumProvider;
    private final MeterRegistry meterRegistry;

    public void downloadDocument(final URL documentLocation) {
        meterRegistry.counter("statistics.document-processed").increment();

        log.debug("Starting to download document {}.", documentLocation);

        if (!documentDownloadEvaluator.evaluateDocumentLocation(documentLocation)) {
            return;
        }

        final String documentId = documentIdFactory.newDocumentId(documentLocation);
        final File stageFileLocation = stageLocationFactory.newLocation(documentId);

        try {
            fileDownloader.downloadFile(documentLocation, stageFileLocation, 30000);
        } catch (FileDownloaderException e) {
            log.debug("Failed to download document!", e);

            documentManipulator.markFailed(documentId);

            return;
        }

        final String checksum = checksumProvider.checksum(documentId);
        final long fileSize = stageFileLocation.length();

        // The ordering like this is for a reason! We don't want to set the file size and checksum values of invalid
        // files!
        if (!documentFileValidator.isValidDocument(documentId)) {
            documentFileManipulator.cleanup(documentId);
            documentManipulator.markInvalid(documentId);

            return;
        }

        // Validate if the file already exists or not. Set the file size and checksum afterwards, even if the file is a
        // duplicate. We can't set it previously because then it will be always found as a "duplicate".
        if (documentEntityFactory.isDocumentExists(checksum, fileSize)) {
            documentFileManipulator.cleanup(documentId);
            documentManipulator.markDuplicate(documentId, fileSize, checksum);

            return;
        }

        try {
            documentFileManipulator.moveToVault(documentId);
        } catch (FileManipulatingException e) {
            log.error("Failed while processing the downloaded document.", e);

            documentManipulator.markProcessFailure(documentId);

            return;
        }

        documentManipulator.markDownloaded(documentId, fileSize, checksum);
    }
}
