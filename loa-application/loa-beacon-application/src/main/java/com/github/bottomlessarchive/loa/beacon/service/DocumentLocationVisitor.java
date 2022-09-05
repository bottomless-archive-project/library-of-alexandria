package com.github.bottomlessarchive.loa.beacon.service;

import com.github.bottomlessarchive.loa.beacon.configuration.BeaconConfigurationProperties;
import com.github.bottomlessarchive.loa.beacon.service.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.bottomlessarchive.loa.url.service.collector.FileCollector;
import com.github.bottomlessarchive.loa.validator.service.DocumentFileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class DocumentLocationVisitor {

    private final FileCollector fileCollector;
    private final DocumentFileValidator documentFileValidator;
    private final StageLocationFactory stageLocationFactory;
    private final BeaconConfigurationProperties beaconConfigurationProperties;

    public Object visitDocumentLocation(final DocumentLocation documentLocation) {
        final StageLocation stageLocation = stageLocationFactory.getLocation(documentLocation.getId(), documentLocation.getType());

        try {
            final URL documentLocationURL = new URL(documentLocation.getLocation());

            fileCollector.acquireFile(documentLocation.getId(), documentLocationURL, stageLocation.getPath(), documentLocation.getType());

            if (documentFileValidator.isValidDocument(documentLocation.getId(), documentLocation.getType())) {
                stageLocation.moveTo(buildStoragePath(documentLocation.getId(), documentLocation.getType()));

                return null; // TODO: return that everything was successful
            } else {
                return null; // TODO: return the failure reason (illegal document)
            }
        } catch (Exception e) {
            return null; // TODO: return the failure reason (IO exception etc)
        } finally {
            if (stageLocation.exists()) {
                stageLocation.cleanup();
            }
        }
    }

    private Path buildStoragePath(final String documentId, final DocumentType documentType) {
        return Path.of(beaconConfigurationProperties.storagePath()).resolve(documentId + "." + documentType.getFileExtension());
    }
}
