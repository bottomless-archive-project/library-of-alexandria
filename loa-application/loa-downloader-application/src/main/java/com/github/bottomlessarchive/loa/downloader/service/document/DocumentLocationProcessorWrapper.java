package com.github.bottomlessarchive.loa.downloader.service.document;

import com.github.bottomlessarchive.loa.downloader.service.document.domain.exception.NotEnoughSpaceException;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.threading.executor.BlockingExecutor;
import com.github.bottomlessarchive.loa.validator.configuration.FileValidationConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * This service is responsible for downloading documents.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentLocationProcessorWrapper {

    private final StageLocationFactory stageLocationFactory;
    private final DocumentLocationProcessorTaskFactory documentLocationProcessorTaskFactory;
    private final FileValidationConfigurationProperties fileValidationConfigurationProperties;

    @Qualifier("downloaderExecutorService")
    private final BlockingExecutor blockingExecutor;

    public void processDocumentLocation(final DocumentLocation documentLocation) {
        processDocumentLocation(documentLocation, null);
    }

    public void processDocumentLocation(final DocumentLocation documentLocation, final Runnable callback) {
        // TODO: This should be somehow synchronized otherwise we might run out of space while processing documents in parallel.
        verifyEnoughStageSpaceIsAvailable();

        blockingExecutor.execute(documentLocationProcessorTaskFactory.newDocumentLocationProcessorTask(documentLocation, callback));
    }

    private void verifyEnoughStageSpaceIsAvailable() {
        if (!stageLocationFactory.hasSpace(fileValidationConfigurationProperties.maximumArchiveSize())) {
            log.error("Not enough local staging space is available!");

            throw new NotEnoughSpaceException("Not enough local staging space is available!");
        }
    }
}
