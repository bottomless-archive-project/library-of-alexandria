package com.github.loa.downloader.service.document;

import com.github.loa.document.service.DocumentTypeCalculator;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.downloader.service.file.DocumentFileManipulator;
import com.github.loa.downloader.service.file.DocumentFileValidator;
import com.github.loa.downloader.service.file.FileCollector;
import com.github.loa.location.service.id.factory.DocumentLocationIdFactory;
import com.github.loa.source.domain.DocumentSourceItem;
import com.github.loa.stage.service.StageLocationFactory;
import com.github.loa.vault.client.service.domain.ArchivingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URL;

/**
 * This service is responsible for downloading documents.
 */
//TODO: This class is responsible for downloading AND (!!!) archiving the document. This is suboptimal.
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentDownloader {

    private final DocumentLocationIdFactory documentLocationIdFactory;
    private final StageLocationFactory stageLocationFactory;
    private final DocumentFileValidator documentFileValidator;
    private final DocumentFileManipulator documentFileManipulator;
    private final FileCollector fileCollector;
    private final DocumentTypeCalculator documentTypeCalculator;

    public Mono<Void> downloadDocument(final DocumentSourceItem documentSourceItem) {
        final URL documentLocation = documentSourceItem.getDocumentLocation();
        final DocumentType documentType = documentTypeCalculator.calculate(documentLocation)
                .orElseThrow(() -> new RuntimeException("Unable to find valid document type for document: "
                        + documentLocation));

        log.debug("Starting to download document {}.", documentLocation);

        return Mono.just(documentLocationIdFactory.newDocumentId(documentLocation))
                .flatMap(documentId -> stageLocationFactory.getLocation(documentId, documentType)
                        .flatMap(stageFileLocation -> fileCollector.acquireFile(documentLocation, stageFileLocation))
                        .flatMap(documentFileLocation -> documentFileValidator.isValidDocument(documentId, documentType)
                                .filter(validationResult -> !validationResult)
                                .flatMap(validationResult -> documentFileManipulator.cleanup(documentFileLocation))
                                .thenReturn(documentFileLocation)
                        )
                        .filter(stageFileLocation -> stageFileLocation.toFile().exists())
                        .flatMap(archivingContext -> documentFileManipulator.moveToVault(
                                ArchivingContext.builder()
                                        .location(documentLocation.toString())
                                        .source(documentSourceItem.getSourceName())
                                        .type(documentType)
                                        .contents(archivingContext)
                                        .build()
                        ))
                );
    }
}
