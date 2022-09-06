package com.github.bottomlessarchive.loa.downloader.service.document;

import com.github.bottomlessarchive.loa.downloader.service.document.domain.exception.NotEnoughSpaceException;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.validator.configuration.FileValidationConfigurationProperties;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

/**
 * This service is responsible for downloading documents.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentLocationProcessorWrapper {

    private final StageLocationFactory stageLocationFactory;
    private final FileValidationConfigurationProperties fileValidationConfigurationProperties;
    private final DocumentLocationProcessor documentLocationProcessor;

    @Qualifier("downloaderSemaphore")
    private final Semaphore downloaderSemaphore;
    @Qualifier("downloaderExecutorService")
    private final ExecutorService downloaderExecutorService;
    @Qualifier("processedDocumentCount")
    private final Counter processedDocumentCount;

    public void processDocumentLocation(final DocumentLocation documentLocation) {
        processDocumentLocation(documentLocation, null);
    }

    public void processDocumentLocation(final DocumentLocation documentLocation, final Runnable callback) {
        verifyEnoughStageSpaceIsAvailable();

        increaseProcessedDocumentCount();

        acquireProcessingPermit();

        downloaderExecutorService.execute(() -> {
            MDC.put("documentLocationId", documentLocation.getId());

            documentLocationProcessor.doProcessDocumentLocation(documentLocation);

            if (callback != null) {
                callback.run();
            }

            MDC.clear();

            releaseProcessingPermit();
        });
    }

    private void verifyEnoughStageSpaceIsAvailable() {
        if (!stageLocationFactory.hasSpace(fileValidationConfigurationProperties.maximumArchiveSize())) {
            log.error("Not enough local staging space is available!");

            throw new NotEnoughSpaceException("Not enough local staging space is available!");
        }
    }

    private void increaseProcessedDocumentCount() {
        processedDocumentCount.increment();
    }

    @SneakyThrows
    private void acquireProcessingPermit() {
        downloaderSemaphore.acquire();
    }

    private void releaseProcessingPermit() {
        downloaderSemaphore.release();
    }
}
