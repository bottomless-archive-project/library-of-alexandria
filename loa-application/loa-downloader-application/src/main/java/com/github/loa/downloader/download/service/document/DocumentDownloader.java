package com.github.loa.downloader.download.service.document;

import com.github.loa.checksum.service.ChecksumProvider;
import com.github.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.loa.document.service.location.id.factory.DocumentLocationIdFactory;
import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.downloader.command.configuration.DownloaderConfigurationProperties;
import com.github.loa.downloader.download.service.file.DocumentFileManipulator;
import com.github.loa.downloader.download.service.file.DocumentFileValidator;
import com.github.loa.downloader.download.service.file.FileDownloader;
import com.github.loa.downloader.download.service.file.domain.FileDownloaderException;
import com.github.loa.downloader.download.service.file.domain.FailedToArchiveException;
import com.github.loa.stage.service.StageLocationFactory;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;

/**
 * This service is responsible for downloading documents.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentDownloader {

    private final DocumentEntityFactory documentEntityFactory;
    private final FileDownloader fileDownloader;
    private final DocumentLocationIdFactory documentLocationIdFactory;
    private final StageLocationFactory stageLocationFactory;
    private final DocumentFileValidator documentFileValidator;
    private final DocumentDownloadEvaluator documentDownloadEvaluator;
    private final DocumentFileManipulator documentFileManipulator;
    private final ChecksumProvider checksumProvider;
    private final MeterRegistry meterRegistry;
    private final DownloaderConfigurationProperties downloaderConfigurationProperties;
    private final CompressionConfigurationProperties compressionConfigurationProperties;

    public void downloadDocument(final URL documentLocation) {
        meterRegistry.counter("statistics.document-processed").increment();

        log.debug("Starting to download document {}.", documentLocation);

        if (!documentDownloadEvaluator.evaluateDocumentLocation(documentLocation)) {
            return;
        }

        final String documentId = documentLocationIdFactory.newDocumentId(documentLocation);

        final DocumentType documentType = Arrays.stream(DocumentType.values())
                .filter(type -> documentLocation.getPath().endsWith("." + type.getFileExtension()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unable to find valid document type for document: "
                        + documentLocation));

        final File stageFileLocation = stageLocationFactory.getLocation(documentId, documentType);

        try {
            fileDownloader.downloadFile(documentLocation, stageFileLocation, 30000);
        } catch (FileDownloaderException e) {
            log.debug("Failed to download document!", e);

            return;
        }

        final String checksum = checksumProvider.checksum(documentId, documentType);
        final long fileSize = stageFileLocation.length();

        if (!documentFileValidator.isValidDocument(documentId, documentType)) {
            documentFileManipulator.cleanup(documentId, documentType);

            return;
        }

        if (documentEntityFactory.isDocumentExists(checksum, fileSize, documentType)) {
            documentFileManipulator.cleanup(documentId, documentType);

            return;
        }

        try {
            final DocumentEntity documentEntity = documentEntityFactory.newDocumentEntity(
                    DocumentCreationContext.builder()
                            .id(documentId)
                            .type(documentType)
                            .location(documentLocation)
                            .status(DocumentStatus.DOWNLOADED)
                            .versionNumber(downloaderConfigurationProperties.getVersionNumber())
                            .compression(compressionConfigurationProperties.getAlgorithm())
                            .checksum(checksum)
                            .fileSize(fileSize)
                            .build()
            );

            documentFileManipulator.moveToVault(documentEntity);
        } catch (FailedToArchiveException e) {
            log.error("Failed while processing the downloaded document.", e);
        }
    }
}
