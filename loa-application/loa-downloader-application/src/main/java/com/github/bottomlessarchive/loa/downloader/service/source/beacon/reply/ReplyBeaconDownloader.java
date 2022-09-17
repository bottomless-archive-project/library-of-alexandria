package com.github.bottomlessarchive.loa.downloader.service.source.beacon.reply;

import com.github.bottomlessarchive.loa.beacon.service.client.BeaconClient;
import com.github.bottomlessarchive.loa.beacon.service.client.domain.BeaconDocumentLocation;
import com.github.bottomlessarchive.loa.downloader.service.document.DocumentLocationEvaluator;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.location.service.id.factory.DocumentLocationIdFactory;
import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentLocationMessage;
import com.github.bottomlessarchive.loa.type.DocumentTypeCalculator;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "loa.downloader.source", havingValue = "beacon-reply")
public class ReplyBeaconDownloader implements CommandLineRunner {

    private final BeaconClient beaconClient;
    private final QueueManipulator queueManipulator;
    private final DocumentTypeCalculator documentTypeCalculator;
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
            final List<DocumentLocation> documentLocationMessages = new ArrayList<>(1000);

            while (documentLocationMessages.size() != 1000) {
                final DocumentLocationMessage documentLocationMessage =
                        queueManipulator.readMessage(Queue.DOCUMENT_LOCATION_QUEUE, DocumentLocationMessage.class);

                final URL documentLocationURL = new URL(documentLocationMessage.getDocumentLocation());

                final DocumentLocation documentLocation = DocumentLocation.builder()
                        .id(documentLocationIdFactory.newDocumentLocationId(documentLocationURL))
                        .location(documentLocationURL)
                        .type(DocumentType.FB2)  //TODO: the type should come from the message
                        .sourceName(documentLocationMessage.getSourceName())
                        .build();

                log.info("Processing location.");

                if (documentLocationEvaluator.shouldProcessDocumentLocation(documentLocation)) {
                    documentLocationMessages.add(documentLocation);
                } else {
                    log.info("Document location is a duplicate.");
                }
            }

            beaconClient.visitDocumentLocations("beacon-1", //TODO: Beacon name!
                    documentLocationMessages.stream()
                            .map(location -> BeaconDocumentLocation.builder()
                                    .id(location.getId())
                                    .type(DocumentType.FB2) //TODO: add type!!!
                                    .location(location.getLocation())
                                    //TODO: Source name?
                                    .build()
                            )
                            .collect(Collectors.toList())
            );
        }
    }
}
