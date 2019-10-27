package com.github.loa.downloader.download.service.document;

import com.github.loa.checksum.service.ChecksumProvider;
import com.github.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.loa.document.service.location.id.factory.DocumentLocationIdFactory;
import com.github.loa.downloader.command.configuration.DownloaderConfigurationProperties;
import com.github.loa.downloader.download.service.file.DocumentFileManipulator;
import com.github.loa.downloader.download.service.file.DocumentFileValidator;
import com.github.loa.downloader.download.service.file.FileDownloader;
import com.github.loa.stage.service.StageLocationFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

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
    private final DownloaderConfigurationProperties downloaderConfigurationProperties;
    private final CompressionConfigurationProperties compressionConfigurationProperties;

    public Mono<Void> downloadDocument(final URL documentLocation) {
        log.debug("Starting to download document {}.", documentLocation);

        return documentDownloadEvaluator.evaluateDocumentLocation(documentLocation)
                .flatMap(shouldDownload -> {
                    if (shouldDownload) {
                        final String documentId = documentLocationIdFactory.newDocumentId(documentLocation);

                        final DocumentType documentType = Arrays.stream(DocumentType.values())
                                .filter(type -> documentLocation.getPath().endsWith("." + type.getFileExtension()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Unable to find valid document type for document: "
                                        + documentLocation));

                        return stageLocationFactory.getLocation(documentId, documentType)
                                .flatMap(stageFileLocation -> fileDownloader.downloadFile(documentLocation, stageFileLocation))
                                .flatMap(documentFileLocation -> documentFileValidator.isValidDocument(documentId, documentType)
                                        .filter(validationResult -> !validationResult)
                                        .flatMap(validationResult -> documentFileManipulator.cleanup(documentId, documentType))
                                        .thenReturn(documentFileLocation)
                                )
                                .map(File::length)
                                .flatMap(fileLength -> checksumProvider.checksum(documentId, documentType)
                                        .flatMap(checksum -> documentEntityFactory.isDocumentExists(checksum, fileLength, documentType))
                                        .filter(documentExists -> documentExists)
                                        .flatMap(documentExists -> documentFileManipulator.cleanup(documentId, documentType))
                                        .thenReturn(fileLength)
                                )
                                .flatMap(fileLength -> checksumProvider.checksum(documentId, documentType)
                                        .flatMap(checksum -> documentEntityFactory.newDocumentEntity(
                                                DocumentCreationContext.builder()
                                                        .id(documentId)
                                                        .type(documentType)
                                                        .location(documentLocation)
                                                        .status(DocumentStatus.DOWNLOADED)
                                                        .versionNumber(downloaderConfigurationProperties.getVersionNumber())
                                                        .compression(compressionConfigurationProperties.getAlgorithm())
                                                        .checksum(checksum)
                                                        .fileSize(fileLength)
                                                        .build()
                                        )
                                                .flatMap(documentFileManipulator::moveToVault))
                                )
                                .then();
                    } else {
                        return Mono.empty();
                    }
                });
    }
}
