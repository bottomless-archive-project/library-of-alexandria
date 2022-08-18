package com.github.bottomlessarchive.loa.downloader.service.source.queue;

import com.github.bottomlessarchive.loa.downloader.service.document.DocumentLocationEvaluator;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.location.domain.link.UrlLink;
import com.github.bottomlessarchive.loa.location.service.id.factory.DocumentLocationIdFactory;
import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.downloader.service.document.DocumentLocationProcessor;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentLocationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "loa.downloader.source", havingValue = "queue", matchIfMissing = true)
public class DownloadQueueListener implements CommandLineRunner {

    private final QueueManipulator queueManipulator;
    private final DocumentLocationProcessor documentLocationProcessor;
    private final DocumentLocationEvaluator documentLocationEvaluator;
    private final DocumentLocationIdFactory documentLocationIdFactory;

    @Override
    public void run(final String... args) throws MalformedURLException {
        queueManipulator.silentlyInitializeQueue(Queue.DOCUMENT_LOCATION_QUEUE);
        if (log.isInfoEnabled()) {
            log.info("Initialized queue processing! There are {} messages available in the location queue!",
                    queueManipulator.getMessageCount(Queue.DOCUMENT_LOCATION_QUEUE));
        }

        queueManipulator.silentlyInitializeQueue(Queue.DOCUMENT_ARCHIVING_QUEUE);
        if (log.isInfoEnabled()) {
            log.info("Initialized queue processing! There are {} messages available in the archiving queue!",
                    queueManipulator.getMessageCount(Queue.DOCUMENT_ARCHIVING_QUEUE));
        }

        while (true) {
            final DocumentLocationMessage documentLocationMessage =
                    queueManipulator.readMessage(Queue.DOCUMENT_LOCATION_QUEUE, DocumentLocationMessage.class);

            final URL documentLocationURL = new URL(documentLocationMessage.getDocumentLocation());

            final String documentLocationId = documentLocationIdFactory.newDocumentLocationId(documentLocationURL);

            MDC.put("documentLocationId", documentLocationId);

            log.info("Starting to process document location: {}.", documentLocationMessage.getDocumentLocation());

            final DocumentLocation documentLocation = DocumentLocation.builder()
                    .id(documentLocationIdFactory.newDocumentLocationId(documentLocationURL))
                    .location(
                            UrlLink.builder()
                                    .url(documentLocationURL)
                                    .build()
                    )
                    .sourceName(documentLocationMessage.getSourceName())
                    .build();

            if (documentLocationEvaluator.shouldProcessDocumentLocation(documentLocation)) {
                documentLocationProcessor.processDocumentLocation(documentLocation);
            } else {
                log.info("Document location is a duplicate.");
            }

            MDC.clear();
        }
    }
}
