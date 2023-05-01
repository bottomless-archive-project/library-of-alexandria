package com.github.bottomlessarchive.loa.downloader.service.source.beacon.reply;

import com.github.bottomlessarchive.loa.beacon.service.client.BeaconClient;
import com.github.bottomlessarchive.loa.beacon.service.client.domain.BeaconDocumentLocation;
import com.github.bottomlessarchive.loa.beacon.service.client.domain.BeaconDocumentLocationResult;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.downloader.service.document.DocumentArchiver;
import com.github.bottomlessarchive.loa.downloader.service.document.domain.DocumentArchivingContext;
import com.github.bottomlessarchive.loa.downloader.service.source.beacon.configuration.BeaconDownloaderConfigurationProperties;
import com.github.bottomlessarchive.loa.downloader.service.source.beacon.service.BeaconDocumentLocationFactory;
import com.github.bottomlessarchive.loa.downloader.service.source.beacon.service.DocumentLocationCollector;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocationResultType;
import com.github.bottomlessarchive.loa.location.service.DocumentLocationManipulator;
import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import com.github.bottomlessarchive.loa.url.service.downloader.domain.DownloadResult;
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
@ConditionalOnProperty(value = "loa.downloader.source", havingValue = "beacon-reply")
public class ReplyBeaconDownloader implements CommandLineRunner {

    private final BeaconClient beaconClient;
    private final QueueManipulator queueManipulator;
    private final DocumentArchiver documentArchiver;
    private final StageLocationFactory stageLocationFactory;
    private final DocumentEntityFactory documentEntityFactory;
    private final DocumentLocationCollector documentLocationCollector;
    private final DocumentLocationManipulator documentLocationManipulator;
    private final BeaconDocumentLocationFactory beaconDocumentLocationFactory;
    private final BeaconDownloaderConfigurationProperties beaconDownloaderConfigurationProperties;

    @Override
    public void run(final String... args) {
        queueManipulator.silentlyInitializeQueues(Queue.DOCUMENT_LOCATION_QUEUE, Queue.DOCUMENT_ARCHIVING_QUEUE);

        while (true) {
            final List<DocumentLocation> documentLocationMessages = documentLocationCollector.collectDocumentsToProcess(
                    beaconDownloaderConfigurationProperties.requestSize());

            log.info("Sending {} locations for processing to the beacon with name: {}.", documentLocationMessages.size(),
                    beaconDownloaderConfigurationProperties.name());

            final List<BeaconDocumentLocationResult> beaconDocumentLocationResults =
                    processDocumentLocationsByBeacon(documentLocationMessages);

            log.info("Got back {} location results from beacon with name: {}.", beaconDocumentLocationResults.size(),
                    beaconDownloaderConfigurationProperties.name());

            beaconDocumentLocationResults.forEach(this::processBeaconDocumentLocationResult);
        }
    }

    private List<BeaconDocumentLocationResult> processDocumentLocationsByBeacon(final List<DocumentLocation> documentLocationMessages) {
        final List<BeaconDocumentLocation> beaconDocumentLocations = documentLocationMessages.stream()
                .map(beaconDocumentLocationFactory::newBeaconDocumentLocation)
                .toList();

        return beaconClient.visitDocumentLocations(beaconDownloaderConfigurationProperties.host(),
                beaconDownloaderConfigurationProperties.port(), beaconDocumentLocations);
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

                beaconClient.deleteDocumentFromBeacon(beaconDownloaderConfigurationProperties.host(),
                        beaconDownloaderConfigurationProperties.port(), documentEntityOptional.get().getId());

                return;
            }

            try (StageLocation stageLocation = stageLocationFactory.getLocation(documentId)) {
                log.info("Downloading document from beacon: {}.", beaconDownloaderConfigurationProperties.name());

                // Download as long as it is not successful
                DownloadResult beaconDownloadResult = null;
                while (beaconDownloadResult != DownloadResult.OK) {
                    beaconDownloadResult = beaconClient.downloadDocumentFromBeacon(
                            beaconDownloaderConfigurationProperties.host(),
                            beaconDownloaderConfigurationProperties.port(), documentId, stageLocation.getPath());
                }

                final DocumentArchivingContext documentArchivingContext = DocumentArchivingContext.builder()
                        .id(documentId)
                        .type(beaconDocumentLocationResult.getType())
                        .source(beaconDocumentLocationResult.getSourceName())
                        .sourceLocationId(beaconDocumentLocationResult.getId())
                        .contents(stageLocation.getPath())
                        .build();

                documentArchiver.archiveDocument(documentArchivingContext);
            } catch (final Exception e) {
                log.info("Error downloading a document: {}!", e.getMessage());
            }
        }
    }
}
