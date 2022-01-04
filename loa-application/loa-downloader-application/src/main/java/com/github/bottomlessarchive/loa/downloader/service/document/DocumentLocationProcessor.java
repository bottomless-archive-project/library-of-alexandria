package com.github.bottomlessarchive.loa.downloader.service.document;

import com.github.bottomlessarchive.loa.document.service.DocumentTypeCalculator;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import com.github.bottomlessarchive.loa.downloader.service.document.domain.DocumentArchivingContext;
import com.github.bottomlessarchive.loa.downloader.service.file.FileCollector;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import com.github.bottomlessarchive.loa.validator.service.DocumentFileValidator;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URL;
import java.util.Optional;
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

    public Mono<Void> processDocumentLocation(final DocumentLocation documentLocation) {
        processedDocumentCount.increment();

        final URL documentLocationURL = documentLocation.getLocation().toUrl().orElseThrow();
        final Optional<DocumentType> documentTypeOptional = documentTypeCalculator.calculate(documentLocationURL);

        if (documentTypeOptional.isEmpty()) {
            log.error("Invalid document location found: {}! This shouldn't normally happen! Please report it to the developers!",
                    documentLocation);

            return Mono.empty();
        }

        final DocumentType documentType = documentTypeOptional.get();

        log.debug("Starting to download document {}.", documentLocationURL);

        return Mono.just(UUID.randomUUID())
                .flatMap(documentId -> stageLocationFactory.getLocation(documentId.toString(), documentType)
                        .flatMap(stageFileLocation -> acquireFile(documentLocationURL, stageFileLocation))
                        .publishOn(Schedulers.parallel())
                        .flatMap(documentFileLocation -> documentFileValidator.isValidDocument(documentId.toString(), documentType)
                                .filter(validationResult -> !validationResult)
                                .flatMap(validationResult -> documentFileLocation.cleanup())
                                .thenReturn(documentFileLocation)
                        )
                        .publishOn(Schedulers.boundedElastic())
                        .filterWhen(StageLocation::exists)
                        .flatMap(stageLocation -> Mono.just(
                                DocumentArchivingContext.builder()
                                        .id(documentId)
                                        .type(documentType)
                                        .source(documentLocation.getSourceName())
                                        .sourceLocationId(documentLocation.getId())
                                        .contents(stageLocation.getPath())
                                        .build()
                                )
                        )
                        .flatMap(documentArchiver::archiveDocument)
                        .flatMap(this::cleanup)
                        .onErrorResume(error -> {
                            if (log.isDebugEnabled()) {
                                log.debug("Error downloading a document: {}!", error.getMessage());
                            }

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
                .flatMap(StageLocation::cleanup);
    }
}
