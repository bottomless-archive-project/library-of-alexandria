package com.github.bottomlessarchive.loa.downloader.service.source.queue;

import com.github.bottomlessarchive.loa.downloader.service.document.DocumentLocationEvaluator;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.downloader.service.document.DocumentLocationProcessorWrapper;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentLocationMessage;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "loa.downloader.source", havingValue = "queue", matchIfMissing = true)
public class DownloadQueueListener implements CommandLineRunner {

    private final QueueManipulator queueManipulator;
    private final DocumentLocationProcessorWrapper documentLocationProcessorWrapper;
    private final DocumentLocationEvaluator documentLocationEvaluator;

    @Override
    public void run(final String... args) throws MalformedURLException {
        queueManipulator.silentlyInitializeQueues(Queue.DOCUMENT_LOCATION_QUEUE, Queue.DOCUMENT_ARCHIVING_QUEUE);

        while (true) {
            final Optional<DocumentLocationMessage> documentLocationMessage =
                    queueManipulator.readMessage(Queue.DOCUMENT_LOCATION_QUEUE, DocumentLocationMessage.class);

            if (documentLocationMessage.isEmpty()) {
                continue;
            }

            final URL documentLocationURL = new URL(documentLocationMessage.get().getDocumentLocation());

            final DocumentLocation documentLocation = DocumentLocation.builder()
                    .id(documentLocationMessage.get().getId())
                    .type(DocumentType.valueOf(documentLocationMessage.get().getType()))
                    .location(documentLocationURL)
                    .sourceName(documentLocationMessage.get().getSourceName())
                    .build();

            log.info("Processing location.");

            if (documentLocationEvaluator.shouldProcessDocumentLocation(documentLocation)) {
                documentLocationProcessorWrapper.processDocumentLocation(documentLocation);
            } else {
                log.info("Document location is a duplicate.");
            }
        }
    }
}
