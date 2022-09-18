package com.github.bottomlessarchive.loa.downloader.service.source.beacon.reply;

import com.github.bottomlessarchive.loa.beacon.service.client.BeaconClient;
import com.github.bottomlessarchive.loa.beacon.service.client.domain.BeaconDocumentLocation;
import com.github.bottomlessarchive.loa.beacon.service.client.domain.BeaconDocumentLocationResult;
import com.github.bottomlessarchive.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.bottomlessarchive.loa.downloader.service.document.DocumentArchiver;
import com.github.bottomlessarchive.loa.downloader.service.document.DocumentIdFactory;
import com.github.bottomlessarchive.loa.downloader.service.document.DocumentLocationEvaluator;
import com.github.bottomlessarchive.loa.downloader.service.document.domain.DocumentArchivingContext;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocationResultType;
import com.github.bottomlessarchive.loa.location.service.DocumentLocationManipulator;
import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentArchivingMessage;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentLocationMessage;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "loa.downloader.source", havingValue = "beacon-reply")
public class ReplyBeaconDownloader implements CommandLineRunner {

    private final BeaconClient beaconClient;
    private final QueueManipulator queueManipulator;
    private final DocumentArchiver documentArchiver;
    private final DocumentIdFactory documentIdFactory;
    private final StageLocationFactory stageLocationFactory;
    private final DocumentLocationEvaluator documentLocationEvaluator;
    private final DocumentLocationManipulator documentLocationManipulator;
    private final CompressionConfigurationProperties compressionConfigurationProperties;

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

                final DocumentLocation documentLocation = DocumentLocation.builder()
                        .id(documentLocationMessage.getId())
                        .location(new URL(documentLocationMessage.getDocumentLocation()))
                        .type(DocumentType.valueOf(documentLocationMessage.getType()))
                        .sourceName(documentLocationMessage.getSourceName())
                        .build();

                log.info("Processing location.");

                if (documentLocationEvaluator.shouldProcessDocumentLocation(documentLocation)) {
                    documentLocationMessages.add(documentLocation);
                } else {
                    log.info("Document location is a duplicate.");
                }
            }

            final List<BeaconDocumentLocationResult> result = beaconClient.visitDocumentLocations("beacon-1", //TODO: Beacon name!
                    documentLocationMessages.stream()
                            .map(location -> BeaconDocumentLocation.builder()
                                    .id(location.getId())
                                    .type(location.getType())
                                    .location(location.getLocation())
                                    .sourceName(location.getSourceName())
                                    .build()
                            )
                            .collect(Collectors.toList())
            );

            for (BeaconDocumentLocationResult beaconDocumentLocationResult : result) {
                // Updating the location result is mandatory
                final DocumentLocationResultType documentLocationResultType = DocumentLocationResultType.valueOf(
                        beaconDocumentLocationResult.getResultType());

                documentLocationManipulator.updateDownloadResultCode(beaconDocumentLocationResult.getId(), documentLocationResultType);

                //TODO: Filter duplicates based on the returned data (so we don't need to download the doc if it is a duplicate)

                if (DocumentLocationResultType.OK.equals(documentLocationResultType)) {
                    final UUID documentId = beaconDocumentLocationResult.getDocumentId()
                            .orElseThrow();

                    final StageLocation stageLocation = stageLocationFactory.getLocation(documentId,
                            beaconDocumentLocationResult.getType());

                    try {
                        beaconClient.downloadDocumentFromBeacon("beacon-1", documentId, stageLocation.getPath()); //TODO: beacon name!

                        final DocumentArchivingContext documentArchivingContext = DocumentArchivingContext.builder()
                                .id(documentId)
                                .type(beaconDocumentLocationResult.getType())
                                .source(beaconDocumentLocationResult.getSourceName())
                                .sourceLocationId(beaconDocumentLocationResult.getId())
                                .contents(stageLocation.getPath())
                                .build();

                        documentArchiver.archiveDocument(documentArchivingContext);
                    } catch (final Exception e) {
                        //TODO: Handle this normally! Ie retry until we can get the document etc!
                        log.info("Error downloading a document: {}!", e.getMessage());
                    } finally {
                        if (stageLocation.exists()) {
                            stageLocation.cleanup();
                        }
                    }
                }
            }
        }
    }
}
