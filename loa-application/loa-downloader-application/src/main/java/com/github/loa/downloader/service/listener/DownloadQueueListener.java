package com.github.loa.downloader.service.listener;

import com.github.loa.downloader.service.DocumentLocationCreationContextFactory;
import com.github.loa.downloader.service.document.DocumentDownloader;
import com.github.loa.location.service.factory.DocumentLocationEntityFactory;
import com.github.loa.location.service.factory.domain.DocumentLocationCreationContext;
import com.github.loa.source.domain.DocumentSourceItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadQueueListener implements CommandLineRunner {

    private final DocumentDownloader documentDownloader;
    private final DocumentLocationEntityFactory documentLocationEntityFactory;
    private final ClientSession clientSession;
    private final DownloaderQueueConsumer downloaderQueueConsumer;
    private final DocumentLocationCreationContextFactory documentLocationCreationContextFactory;

    @Override
    public void run(String... args) {
        Flux.generate(downloaderQueueConsumer)
                .doFirst(this::initializeProcessing)
                .flatMap(this::evaluateDocumentLocation)
                .flatMap(this::downloadDocument)
                //TODO: Move the archiving logic here out from the document downloader!
                .doFinally(this::finishProcessing)
                .subscribe();
    }

    private Mono<DocumentSourceItem> evaluateDocumentLocation(final DocumentSourceItem documentSourceItem) {
        final DocumentLocationCreationContext documentLocationCreationContext =
                documentLocationCreationContextFactory.newCreatingContext(documentSourceItem);

        return documentLocationEntityFactory.isDocumentLocationExistsOrCreate(documentLocationCreationContext)
                .filter(exists -> !exists)
                .thenReturn(documentSourceItem);
    }

    private Mono<Void> downloadDocument(final DocumentSourceItem documentSourceItem) {
        return documentDownloader.downloadDocument(documentSourceItem)
                .onErrorResume(error -> Mono.just(error)
                        .doOnNext(throwable -> log.debug("Error downloading a document: {}!", throwable.getMessage()))
                        .then()
                );
    }

    private void initializeProcessing() {
        try {
            clientSession.start();
        } catch (ActiveMQException e) {
            e.printStackTrace();
        }
    }

    private void finishProcessing(final SignalType signalType) {
        try {
            clientSession.close();
        } catch (ActiveMQException e) {
            e.printStackTrace();
        }
    }
}
