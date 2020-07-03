package com.github.loa.downloader.service.document;

import com.github.loa.document.service.DocumentTypeCalculator;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.downloader.service.file.DocumentFileValidator;
import com.github.loa.downloader.service.file.FileCollector;
import com.github.loa.source.domain.DocumentSourceItem;
import com.github.loa.stage.service.StageLocationFactory;
import com.github.loa.stage.service.domain.StageLocation;
import com.github.loa.vault.client.service.domain.DocumentArchivingContext;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URL;
import java.util.UUID;

/**
 * This service is responsible for downloading documents.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentLocationProcessor {

    private final StageLocationFactory stageLocationFactory;
    private final DocumentFileValidator documentFileValidator;
    private final FileCollector fileCollector;
    private final DocumentArchiver documentArchiver;
    private final DocumentTypeCalculator documentTypeCalculator;

    @Qualifier("processedDocumentCount")
    private final Counter processedDocumentCount;

    public Mono<Void> processDocumentLocation(final DocumentSourceItem documentSourceItem) {
        processedDocumentCount.increment();

        final URL documentLocation = documentSourceItem.getDocumentLocation();
        final DocumentType documentType = documentTypeCalculator.calculate(documentLocation)
                .orElseThrow(() -> new RuntimeException("Unable to find valid document type for document: "
                        + documentLocation));

        log.debug("Starting to download document {}.", documentLocation);

        return Mono.just(UUID.randomUUID())
                .flatMap(documentId -> stageLocationFactory.getLocation(documentId.toString(), documentType)
                        .flatMap(stageFileLocation -> acquireFile(documentLocation, stageFileLocation))
                        .publishOn(Schedulers.parallel())
                        .flatMap(documentFileLocation -> documentFileValidator.isValidDocument(documentId.toString(), documentType)
                                .filter(validationResult -> !validationResult)
                                .doOnNext(validationResult -> documentFileLocation.cleanup())
                                .thenReturn(documentFileLocation)
                        )
                        .publishOn(Schedulers.boundedElastic())
                        .filter(StageLocation::exists)
                        .flatMap(stageLocation -> Mono.just(
                                DocumentArchivingContext.builder()
                                        .id(documentId)
                                        .source(documentSourceItem.getSourceName())
                                        .type(documentType)
                                        .contents(stageLocation.getPath())
                                        .build()
                                )
                        )
                        .flatMap(documentArchiver::archiveDocument)
                        .flatMap(this::cleanup)
                        .onErrorResume(error -> {
                            log.debug("Error downloading a document: {}!", error.getMessage());

                            return Mono.empty();
                        })
                );
    }

    private Mono<StageLocation> acquireFile(final URL documentLocation, final StageLocation stageLocation) {
        return fileCollector.acquireFile(documentLocation, stageLocation.getPath())
                .thenReturn(stageLocation);
    }

    private Mono<Void> cleanup(final DocumentArchivingContext documentArchivingContext) {
        return stageLocationFactory.getLocation(documentArchivingContext.getId().toString(), documentArchivingContext.getType())
                .doOnNext(StageLocation::cleanup)
                .then();
    }
}
