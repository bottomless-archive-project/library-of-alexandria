package com.github.bottomlessarchive.loa.beacon.service;

import com.github.bottomlessarchive.loa.beacon.configuration.BeaconConfigurationProperties;
import com.github.bottomlessarchive.loa.beacon.service.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.beacon.service.domain.DocumentLocationResult;
import com.github.bottomlessarchive.loa.checksum.service.ChecksumProvider;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.bottomlessarchive.loa.url.service.collector.FileCollector;
import com.github.bottomlessarchive.loa.url.service.downloader.DocumentLocationResultCalculator;
import com.github.bottomlessarchive.loa.url.service.downloader.domain.DownloadResult;
import com.github.bottomlessarchive.loa.validator.service.DocumentFileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentLocationVisitor {

    private final FileCollector fileCollector;
    private final ChecksumProvider checksumProvider;
    private final DocumentFileValidator documentFileValidator;
    private final StageLocationFactory stageLocationFactory;
    private final BeaconConfigurationProperties beaconConfigurationProperties;
    private final DocumentLocationResultCalculator documentLocationResultCalculator;

    public DocumentLocationResult visitDocumentLocation(final DocumentLocation documentLocation) {
        final UUID documentId = UUID.randomUUID();

        final StageLocation stageLocation = stageLocationFactory.getLocation(documentId, documentLocation.getType());

        try {
            final URL documentLocationURL = new URL(documentLocation.getLocation());

            final DownloadResult documentLocationResultType = fileCollector.acquireFile(documentLocationURL,
                    stageLocation.getPath(), documentLocation.getType());

            if (documentFileValidator.isValidDocument(documentId, stageLocation, documentLocation.getType())) {
                final long size = stageLocation.size();

                try (InputStream inputStream = stageLocation.openStream()) {
                    final String checksum = checksumProvider.checksum(inputStream);

                    return DocumentLocationResult.builder()
                            .id(documentLocation.getId())
                            .documentId(documentId)
                            .resultType(documentLocationResultType)
                            .size(size)
                            .checksum(checksum)
                            .sourceName(documentLocation.getSourceName())
                            .type(documentLocation.getType())
                            .build();
                } finally {
                    stageLocation.moveTo(buildStoragePath(documentId, documentLocation.getType()));
                }
            } else {
                return DocumentLocationResult.builder()
                        .id(documentLocation.getId())
                        .size(-1)
                        .resultType(DownloadResult.INVALID)
                        .build();
            }
        } catch (Exception e) {
            return DocumentLocationResult.builder()
                    .id(documentLocation.getId())
                    .size(-1)
                    .resultType(documentLocationResultCalculator.transformExceptionToDownloadResult(e))
                    .build();
        } finally {
            if (stageLocation.exists()) {
                stageLocation.cleanup();
            }
        }
    }

    private Path buildStoragePath(final UUID documentId, final DocumentType documentType) {
        return Path.of(beaconConfigurationProperties.storagePath()).resolve(documentId + "." + documentType.getFileExtension());
    }
}
