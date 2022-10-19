package com.github.bottomlessarchive.loa.downloader.service.document;

import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.threading.task.CallbackWrapperTask;
import com.github.bottomlessarchive.loa.threading.task.CounterIncrementingWrapperTask;
import com.github.bottomlessarchive.loa.threading.task.MDCWrapperTask;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class DocumentLocationProcessorTaskFactory {

    private final DocumentLocationProcessor documentLocationProcessor;
    @Qualifier("processedDocumentCount")
    private final Counter processedDocumentCount;

    public Runnable newDocumentLocationProcessorTask(final DocumentLocation documentLocation, final Runnable callback) {
        return CounterIncrementingWrapperTask.builder()
                .counter(processedDocumentCount)
                .task(
                        CallbackWrapperTask.builder()
                                .callback(callback)
                                .task(
                                        MDCWrapperTask.builder()
                                                .mdcParameters(Collections.singletonMap("documentLocationId", documentLocation.getId()))
                                                .task(new DocumentLocationProcessorTask(documentLocation, documentLocationProcessor))
                                                .build()
                                )
                                .build()
                )
                .build();
    }
}
