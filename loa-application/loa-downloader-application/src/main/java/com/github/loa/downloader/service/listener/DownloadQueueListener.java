package com.github.loa.downloader.service.listener;

import com.github.loa.downloader.configuration.DownloaderConfigurationProperties;
import com.github.loa.downloader.service.document.DocumentDownloader;
import com.github.loa.location.service.factory.DocumentLocationEntityFactory;
import com.github.loa.location.service.factory.domain.DocumentLocationCreationContext;
import com.github.loa.location.service.id.factory.DocumentLocationIdFactory;
import com.github.loa.source.domain.DocumentSourceItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadQueueListener implements CommandLineRunner {

    private final DocumentDownloader documentDownloader;
    private final DocumentLocationIdFactory documentLocationIdFactory;
    private final DocumentLocationEntityFactory documentLocationEntityFactory;
    private final DownloaderConfigurationProperties downloaderConfigurationProperties;
    private final ClientSessionFactory clientSessionFactory;

    public void run(String... args) throws ActiveMQException, MalformedURLException {
        final ClientSession clientSession = clientSessionFactory.createSession();

        ClientConsumer consumer = clientSession.createConsumer("loa-document-location");

        clientSession.start();

        while (true) {
            ClientMessage msgReceived = consumer.receive();

            final DocumentSourceItem documentSourceItem = DocumentSourceItem.builder()
                    .sourceName(msgReceived.getBodyBuffer().readString())
                    .documentLocation(new URL(msgReceived.getBodyBuffer().readString()))
                    .build();

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
}
