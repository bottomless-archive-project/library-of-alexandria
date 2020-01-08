package com.github.loa.downloader.service.listener;

import com.github.loa.downloader.service.document.DocumentArchiver;
import com.github.loa.downloader.service.document.DocumentDownloader;
import com.github.loa.downloader.service.document.DocumentLocationEvaluator;
import com.github.loa.queue.service.QueueManipulator;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.source.domain.DocumentSourceItem;
import com.github.loa.stage.service.StageLocationFactory;
import com.github.loa.stage.service.domain.StageLocation;
import com.github.loa.vault.client.service.domain.DocumentArchivingContext;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadQueueListener implements CommandLineRunner {

    private final DocumentDownloader documentDownloader;
    private final StageLocationFactory stageLocationFactory;
    private final DownloaderQueueConsumer downloaderQueueConsumer;
    private final QueueManipulator queueManipulator;
    private final DocumentArchiver documentArchiver;
    private final DocumentLocationEvaluator documentLocationEvaluator;

    @Override
    public void run(final String... args) {
        queueManipulator.silentlyInitializeQueue(Queue.DOCUMENT_LOCATION_QUEUE);
        log.info("Initialized queue processing! There are {} messages available in the location queue!",
                queueManipulator.getMessageCount(Queue.DOCUMENT_LOCATION_QUEUE));

        queueManipulator.silentlyInitializeQueue(Queue.DOCUMENT_ARCHIVING_QUEUE);
        log.info("Initialized queue processing! There are {} messages available in the archiving queue!",
                queueManipulator.getMessageCount(Queue.DOCUMENT_LOCATION_QUEUE));

        Flux.generate(downloaderQueueConsumer)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(documentLocationEvaluator::evaluateDocumentLocation)
                .flatMap(documentDownloader::downloadDocument)
                .doOnNext(documentArchiver::archiveDocument)
                .flatMap(this::cleanup)
                .subscribe();
    }

    private Mono<Void> cleanup(final DocumentArchivingContext documentArchivingContext) {
        return stageLocationFactory.getLocation(documentArchivingContext.getId(), documentArchivingContext.getType())
                .doOnNext(StageLocation::cleanup)
                .then();
    }
}
