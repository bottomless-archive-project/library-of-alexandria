package com.github.bottomlessarchive.loa.beacon.service;

import com.github.bottomlessarchive.loa.beacon.service.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.beacon.service.domain.DocumentLocationResult;
import com.github.bottomlessarchive.loa.checksum.service.ChecksumProvider;
import com.github.bottomlessarchive.loa.io.service.collector.DocumentCollector;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import com.github.bottomlessarchive.loa.url.service.downloader.DocumentLocationResultCalculator;
import com.github.bottomlessarchive.loa.url.service.downloader.domain.DownloadResult;
import com.github.bottomlessarchive.loa.validator.service.DocumentFileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentLocationVisitor {

    private final DocumentCollector documentCollector;
    private final ChecksumProvider checksumProvider;
    private final StoragePathFactory storagePathFactory;
    private final DocumentFileValidator documentFileValidator;
    private final StageLocationFactory stageLocationFactory;
    private final DocumentLocationResultCalculator documentLocationResultCalculator;

    public DocumentLocationResult visitDocumentLocation(final DocumentLocation documentLocation) {
        final UUID documentId = UUID.randomUUID();

        final StageLocation stageLocation = stageLocationFactory.getLocation(documentId);

        try {
            final URL documentLocationURL = new URL(documentLocation.getLocation());

            final DownloadResult documentLocationResultType = documentCollector.acquireDocument(documentLocationURL,
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
                    stageLocation.moveTo(storagePathFactory.buildStoragePath(documentId));
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
}
