package com.github.loa.downloader.service.source.queue;

import com.github.loa.downloader.service.document.DocumentLocationEvaluator;
import com.github.loa.downloader.service.document.DocumentLocationProcessor;
import com.github.loa.queue.service.QueueManipulator;
import com.github.loa.queue.service.domain.Queue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "loa.downloader.source", havingValue = "queue", matchIfMissing = true)
public class DownloadQueueListener implements CommandLineRunner {

    private final QueueManipulator queueManipulator;
    private final DocumentLocationProcessor documentLocationProcessor;
    private final DownloaderQueueConsumer downloaderQueueConsumer;
    private final DocumentLocationEvaluator documentLocationEvaluator;

    @Override
    public void run(final String... args) {
        queueManipulator.silentlyInitializeQueue(Queue.DOCUMENT_LOCATION_QUEUE);
        log.info("Initialized queue processing! There are {} messages available in the location queue!",
                queueManipulator.getMessageCount(Queue.DOCUMENT_LOCATION_QUEUE));

        queueManipulator.silentlyInitializeQueue(Queue.DOCUMENT_ARCHIVING_QUEUE);
        log.info("Initialized queue processing! There are {} messages available in the archiving queue!",
                queueManipulator.getMessageCount(Queue.DOCUMENT_ARCHIVING_QUEUE));

        Flux.generate(downloaderQueueConsumer)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(documentLocationEvaluator::evaluateDocumentLocation)
                .flatMap(documentLocationProcessor::processDocumentLocation)
                .subscribe();
    }
}
