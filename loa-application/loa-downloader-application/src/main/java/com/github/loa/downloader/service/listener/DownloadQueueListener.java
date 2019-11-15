package com.github.loa.downloader.service.listener;

import com.github.loa.downloader.configuration.DownloaderConfigurationProperties;
import com.github.loa.downloader.service.document.DocumentDownloader;
import com.github.loa.location.service.factory.DocumentLocationEntityFactory;
import com.github.loa.location.service.factory.domain.DocumentLocationCreationContext;
import com.github.loa.location.service.id.factory.DocumentLocationIdFactory;
import com.github.loa.source.domain.DocumentSourceItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadQueueListener {

    private final DocumentDownloader documentDownloader;
    private final DocumentLocationIdFactory documentLocationIdFactory;
    private final DocumentLocationEntityFactory documentLocationEntityFactory;
    private final DownloaderConfigurationProperties downloaderConfigurationProperties;

    @JmsListener(destination = "loa.downloader", concurrency = "10-100")
    public void receive(final Message<DocumentSourceItem> message) {
        final DocumentSourceItem documentSourceItem = message.getPayload();

        final String documentId = documentLocationIdFactory.newDocumentId(documentSourceItem.getDocumentLocation());

        final DocumentLocationCreationContext documentLocationCreationContext =
                DocumentLocationCreationContext.builder()
                        .id(documentId)
                        .url(documentSourceItem.getDocumentLocation().toString())
                        .source(documentSourceItem.getSourceName())
                        .downloaderVersion(downloaderConfigurationProperties.getVersionNumber())
                        .build();

        documentLocationEntityFactory.isDocumentLocationExistsOrCreate(documentLocationCreationContext)
                .filter(exists -> !exists)
                .map(exists -> documentSourceItem)
                .flatMap(documentDownloader::downloadDocument)
                .onErrorResume(error -> Mono.just(error)
                        .doOnNext(throwable -> log.debug("Error downloading a document: {}!", throwable.getMessage()))
                        .then()
                )
                .block();
    }
}
