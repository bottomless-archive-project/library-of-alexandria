package com.github.loa.downloader.service.listener;

import com.github.loa.downloader.service.DocumentLocationCreationContextFactory;
import com.github.loa.downloader.service.document.DocumentDownloader;
import com.github.loa.location.service.factory.DocumentLocationEntityFactory;
import com.github.loa.location.service.factory.domain.DocumentLocationCreationContext;
import com.github.loa.queue.service.QueueManipulator;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.message.DocumentArchivingMessage;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadQueueListener implements CommandLineRunner {

    private final DocumentDownloader documentDownloader;
    private final StageLocationFactory stageLocationFactory;
    private final DocumentLocationEntityFactory documentLocationEntityFactory;
    private final DownloaderQueueConsumer downloaderQueueConsumer;
    private final DocumentLocationCreationContextFactory documentLocationCreationContextFactory;
    private final QueueManipulator queueManipulator;

    @Qualifier("processedDocumentCount")
    private final Counter processedDocumentCount;
    @Qualifier("archivedDocumentCount")
    private final Counter archivedDocumentCount;

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
                .flatMap(this::evaluateDocumentLocation)
                .doOnNext(this::incrementProcessedCount)
                .flatMap(this::downloadDocument)
                .doOnNext(this::archiveDocument)
                .flatMap(this::cleanup)
                .subscribe();
    }

    private void incrementProcessedCount(final DocumentSourceItem documentSourceItem) {
        processedDocumentCount.increment();
    }

    private Mono<DocumentSourceItem> evaluateDocumentLocation(final DocumentSourceItem documentSourceItem) {
        final DocumentLocationCreationContext documentLocationCreationContext =
                documentLocationCreationContextFactory.newCreatingContext(documentSourceItem);

        return documentLocationEntityFactory.isDocumentLocationExistsOrCreate(documentLocationCreationContext)
                .filter(exists -> !exists)
                .thenReturn(documentSourceItem);
    }

    private Mono<DocumentArchivingContext> downloadDocument(final DocumentSourceItem documentSourceItem) {
        return documentDownloader.downloadDocument(documentSourceItem);
    }

    private void archiveDocument(final DocumentArchivingContext documentArchivingContext) {
        try (final InputStream documentContent = new FileInputStream(documentArchivingContext.getContents().toFile())) {
            archivedDocumentCount.increment();

            queueManipulator.sendMessage(Queue.DOCUMENT_ARCHIVING_QUEUE,
                    DocumentArchivingMessage.builder()
                            .type(documentArchivingContext.getType().toString())
                            .source(documentArchivingContext.getSource())
                            .content(documentContent.readAllBytes())
                            .build()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to send document for archiving!", e);
        }
    }

    private Mono<Void> cleanup(final DocumentArchivingContext documentArchivingContext) {
        return stageLocationFactory.getLocation(documentArchivingContext.getId(), documentArchivingContext.getType())
                .doOnNext(StageLocation::cleanup)
                .then();
    }
}
