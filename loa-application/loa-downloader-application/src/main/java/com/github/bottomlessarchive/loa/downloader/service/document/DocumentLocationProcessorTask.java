package com.github.bottomlessarchive.loa.downloader.service.document;

import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocumentLocationProcessorTask implements Runnable {

    private final DocumentLocation documentLocation;
    private final DocumentLocationProcessingExecutor documentLocationProcessingExecutor;

    @Override
    public void run() {
        documentLocationProcessingExecutor.executeProcessing(documentLocation);
    }
}
