package com.github.bottomlessarchive.loa.downloader.service.source.beacon.accumulating;

import com.github.bottomlessarchive.loa.beacon.service.client.BeaconClient;
import com.github.bottomlessarchive.loa.beacon.service.client.domain.BeaconDocumentLocation;
import com.github.bottomlessarchive.loa.beacon.service.client.domain.BeaconDocumentLocationResult;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.bottomlessarchive.loa.downloader.configuration.DownloaderConfigurationProperties;
import com.github.bottomlessarchive.loa.downloader.service.source.beacon.configuration.BeaconDownloaderConfigurationProperties;
import com.github.bottomlessarchive.loa.downloader.service.source.beacon.service.BeaconDocumentLocationFactory;
import com.github.bottomlessarchive.loa.downloader.service.source.beacon.service.DocumentLocationCollector;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocationResultType;
import com.github.bottomlessarchive.loa.location.service.DocumentLocationManipulator;
import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "loa.downloader.source", havingValue = "beacon-accumulating")
public class AccumulatingBeaconDownloader implements CommandLineRunner {

    private final BeaconClient beaconClient;
    private final QueueManipulator queueManipulator;
    private final DocumentEntityFactory documentEntityFactory;
    private final DocumentLocationCollector documentLocationCollector;
    private final DocumentLocationManipulator documentLocationManipulator;
    private final BeaconDocumentLocationFactory beaconDocumentLocationFactory;
    private final DownloaderConfigurationProperties downloaderConfigurationProperties;
    private final BeaconDownloaderConfigurationProperties beaconDownloaderConfigurationProperties;

    @Override
    public void run(final String... args) {
        queueManipulator.silentlyInitializeQueues(Queue.DOCUMENT_LOCATION_QUEUE, Queue.DOCUMENT_ARCHIVING_QUEUE);

        while (true) {
            final List<DocumentLocation> documentLocationMessages = documentLocationCollector.collectDocumentsToProcess(
                    beaconDownloaderConfigurationProperties.requestSize());

            log.info("Sending {} locations for processing to the beacon with name: {}.", documentLocationMessages.size(),
                    beaconDownloaderConfigurationProperties.activeBeacon());

            final List<BeaconDocumentLocationResult> beaconDocumentLocationResults =
                    processDocumentLocationsByBeacon(documentLocationMessages);

            log.info("Got back {} location results from beacon with name: {}.", beaconDocumentLocationResults.size(),
                    beaconDownloaderConfigurationProperties.activeBeacon());

            beaconDocumentLocationResults.forEach(this::processBeaconDocumentLocationResult);
        }
    }

    private List<BeaconDocumentLocationResult> processDocumentLocationsByBeacon(final List<DocumentLocation> documentLocationMessages) {
        final List<BeaconDocumentLocation> beaconDocumentLocations = documentLocationMessages.stream()
                .map(beaconDocumentLocationFactory::newBeaconDocumentLocation)
                .toList();

        return beaconClient.visitDocumentLocations(beaconDownloaderConfigurationProperties.activeBeacon(), beaconDocumentLocations);
    }

    private void processBeaconDocumentLocationResult(final BeaconDocumentLocationResult beaconDocumentLocationResult) {
        // Updating the location result is mandatory
        final DocumentLocationResultType documentLocationResultType = DocumentLocationResultType.valueOf(
                beaconDocumentLocationResult.getResultType());

        documentLocationManipulator.updateDownloadResultCode(beaconDocumentLocationResult.getId(), documentLocationResultType);

        if (DocumentLocationResultType.OK.equals(documentLocationResultType)) {
            final UUID documentId = beaconDocumentLocationResult.getDocumentId()
                    .orElseThrow();

            final Optional<DocumentEntity> documentEntityOptional = documentEntityFactory.getDocumentEntity(
                    beaconDocumentLocationResult.getChecksum(), beaconDocumentLocationResult.getSize(),
                    beaconDocumentLocationResult.getType().name());
            if (documentEntityOptional.isPresent()) {
                log.info("Document with id: {} is a duplicate.", documentId);

                documentEntityFactory.addSourceLocation(documentEntityOptional.get().getId(), documentId.toString());

                beaconClient.deleteDocumentFromBeacon(beaconDownloaderConfigurationProperties.activeBeacon(),
                        documentEntityOptional.get().getId());

                return;
            }

            //TODO: Add a loader that inserts documents into the vault that are coming from an accumulating beacon's folder

            documentEntityFactory.newDocumentEntity(
                    DocumentCreationContext.builder()
                            .id(beaconDocumentLocationResult.getDocumentId().get())
                            .type(beaconDocumentLocationResult.getType())
                            .status(DocumentStatus.ON_BEACON)
                            .beacon(beaconDownloaderConfigurationProperties.activeBeacon())
                            .source(beaconDocumentLocationResult.getSourceName())
                            .sourceLocationId(Optional.of(beaconDocumentLocationResult.getId()))
                            .versionNumber(downloaderConfigurationProperties.versionNumber())
                            .checksum(beaconDocumentLocationResult.getChecksum())
                            .fileSize(beaconDocumentLocationResult.getSize())
                            .build()
            );
        }
    }
}
