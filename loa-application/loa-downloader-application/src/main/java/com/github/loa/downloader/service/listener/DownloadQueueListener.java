package com.github.loa.downloader.service.listener;

import com.github.loa.downloader.service.DocumentLocationCreationContextFactory;
import com.github.loa.downloader.service.document.DocumentDownloader;
import com.github.loa.downloader.service.file.DocumentFileManipulator;
import com.github.loa.location.service.factory.DocumentLocationEntityFactory;
import com.github.loa.location.service.factory.domain.DocumentLocationCreationContext;
import com.github.loa.source.domain.DocumentSourceItem;
import com.github.loa.vault.client.service.domain.ArchivingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadQueueListener implements CommandLineRunner {

    private final ClientSession clientSession;
    private final DocumentDownloader documentDownloader;
    private final DocumentLocationEntityFactory documentLocationEntityFactory;
    private final DownloaderQueueConsumer downloaderQueueConsumer;
    private final DocumentFileManipulator documentFileManipulator;
    private final DocumentLocationCreationContextFactory documentLocationCreationContextFactory;
    private final AtomicLong processedCount = new AtomicLong();
    private final AtomicLong archivedCount = new AtomicLong();

    @Override
    public void run(final String... args) throws ActiveMQException {
        final ClientSession.QueueQuery queueQuery = clientSession.queueQuery(
                SimpleString.toSimpleString("loa-document-location"));

        log.info("Initialized queue processing! There are {} messages available in the queue!",
                queueQuery.getMessageCount());

        Flux.generate(downloaderQueueConsumer)
                .doFirst(this::initializeProcessing)
                .flatMap(this::evaluateDocumentLocation)
                .groupBy(this::parseDocumentDomain, 1000)
                .flatMap(documentSourceItem1 -> documentSourceItem1
                        .delayElements(Duration.ofSeconds(1))
                        .doOnNext(this::incrementProcessedCount)
                        .flatMap(this::downloadDocument)
                        .flatMap(this::archiveDocument)
                        .doOnNext(this::incrementArchivedCount)
                )
                .doFinally(this::finishProcessing)
                .subscribe();
    }

    private void incrementProcessedCount(final DocumentSourceItem documentSourceItem) {
        final long result = processedCount.incrementAndGet();

        if (result % 100 == 0) {
            log.info("Processed {} urls!", result);
        }
    }

    private void incrementArchivedCount(final Void documentSourceItem) {
        final long result = archivedCount.incrementAndGet();

        if (result % 10 == 0) {
            log.info("Archived {} urls!", result);
        }
    }

    private Mono<DocumentSourceItem> evaluateDocumentLocation(final DocumentSourceItem documentSourceItem) {
        final DocumentLocationCreationContext documentLocationCreationContext =
                documentLocationCreationContextFactory.newCreatingContext(documentSourceItem);

        return documentLocationEntityFactory.isDocumentLocationExistsOrCreate(documentLocationCreationContext)
                .filter(exists -> !exists)
                .thenReturn(documentSourceItem);
    }

    private String parseDocumentDomain(final DocumentSourceItem documentSourceItem) {
        return documentSourceItem.getDocumentLocation().getHost();
    }

    private Mono<ArchivingContext> downloadDocument(final DocumentSourceItem documentSourceItem) {
        return documentDownloader.downloadDocument(documentSourceItem);
    }

    private Mono<Void> archiveDocument(final ArchivingContext archivingContext) {
        return documentFileManipulator.moveToVault(archivingContext);
    }

    private void initializeProcessing() {
        try {
            clientSession.start();
        } catch (ActiveMQException e) {
            log.error("Connection error with the Queue Application!", e);
        }
    }

    private void finishProcessing(final SignalType signalType) {
        try {
            clientSession.close();
        } catch (ActiveMQException e) {
            log.error("Connection error with the Queue Application!", e);
        }
    }
}
