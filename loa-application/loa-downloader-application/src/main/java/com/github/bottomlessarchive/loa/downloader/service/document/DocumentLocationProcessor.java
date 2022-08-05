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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

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

    @Qualifier("downloaderSemaphore")
    private final Semaphore downloaderSemaphore;
    @Qualifier("downloaderExecutorService")
    private final ExecutorService downloaderExecutorService;
    @Qualifier("processedDocumentCount")
    private final Counter processedDocumentCount;

    public void processDocumentLocation(final DocumentLocation documentLocation) {
        processDocumentLocation(documentLocation, null);
    }

    @SneakyThrows
    public void processDocumentLocation(final DocumentLocation documentLocation, final Runnable callback) {
        processedDocumentCount.increment();

        downloaderSemaphore.acquire();

        downloaderExecutorService.execute(() -> {
            doProcessDocumentLocation(documentLocation);

            if (callback != null) {
                callback.run();
            }
        });
    }

    private void doProcessDocumentLocation(final DocumentLocation documentLocation) {
        final URL documentLocationURL = documentLocation.getLocation().toUrl().orElseThrow();
        final Optional<DocumentType> documentTypeOptional = documentTypeCalculator.calculate(documentLocationURL);

        if (documentTypeOptional.isEmpty()) {
            log.debug("Document on location {} has an unknown document type!", documentLocation);

            return;
        }

        final DocumentType documentType = documentTypeOptional.get();

        log.debug("Starting to download document {}.", documentLocationURL);

        final UUID documentId = UUID.randomUUID();

        final StageLocation stageLocation = stageLocationFactory.getLocation(documentId.toString(), documentType);

        try {
            fileCollector.acquireFile(documentLocation.getId(), documentLocationURL, stageLocation.getPath(), documentType);

            if (documentFileValidator.isValidDocument(documentId.toString(), documentType)) {
                final DocumentArchivingContext documentArchivingContext = DocumentArchivingContext.builder()
                        .id(documentId)
                        .type(documentType)
                        .source(documentLocation.getSourceName())
                        .sourceLocationId(documentLocation.getId())
                        .contents(stageLocation.getPath())
                        .build();

                documentArchiver.archiveDocument(documentArchivingContext);
            }
        } catch (final Exception e) {
            log.debug("Error downloading a document: {}!", e.getMessage());
        }

        if (stageLocation.exists()) {
            stageLocation.cleanup();
        }

        downloaderSemaphore.release();
    }
}
