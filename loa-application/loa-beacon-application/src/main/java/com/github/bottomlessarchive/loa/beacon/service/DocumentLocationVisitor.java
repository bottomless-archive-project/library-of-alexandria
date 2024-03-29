package com.github.bottomlessarchive.loa.beacon.service;

import com.github.bottomlessarchive.loa.beacon.service.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.beacon.service.domain.DocumentLocationResult;
import com.github.bottomlessarchive.loa.checksum.service.ChecksumProvider;
import com.github.bottomlessarchive.loa.io.service.collector.DocumentCollector;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import com.github.bottomlessarchive.loa.stage.service.domain.exception.StageAccessException;
import com.github.bottomlessarchive.loa.url.service.downloader.DocumentLocationResultCalculator;
import com.github.bottomlessarchive.loa.url.service.downloader.domain.DownloadResult;
import com.github.bottomlessarchive.loa.validator.service.DocumentFileValidator;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Consumer;

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
        return visitDocumentLocation(documentLocation,
                persistenceEntity -> persistenceEntity.stageLocation().moveTo(persistenceEntity.storagePath()),
                unused -> {
                }
        );
    }

    public DocumentLocationResult visitDocumentLocation(final DocumentLocation documentLocation,
            final Consumer<PersistenceEntity> persistingCallback, final Consumer<FailureEntity> failureCallback) {
        final UUID documentId = UUID.randomUUID();

        try (StageLocation stageLocation = stageLocationFactory.getLocation(documentId)) {
            final DownloadResult documentLocationResultType = documentCollector.acquireDocument(documentLocation.getLocation(),
                    stageLocation.getPath(), documentLocation.getType());

            if (documentFileValidator.isValidDocument(documentId, stageLocation, documentLocation.getType())) {
                final long size = stageLocation.size();

                try (InputStream inputStream = stageLocation.openStream()) {
                    final String checksum = checksumProvider.checksum(inputStream);

                    final DocumentLocationResult documentLocationResult = DocumentLocationResult.builder()
                            .id(documentId.toString())
                            .documentId(documentId)
                            .resultType(documentLocationResultType)
                            .size(size)
                            .checksum(checksum)
                            .sourceName(documentLocation.getSourceName())
                            .type(documentLocation.getType())
                            .build();

                    try {
                        return documentLocationResult;
                    } finally {
                        persistingCallback.accept(
                                PersistenceEntity.builder()
                                        .documentLocation(documentLocation)
                                        .documentLocationResult(documentLocationResult)
                                        .stageLocation(stageLocation)
                                        .storagePath(storagePathFactory.buildStoragePath(documentId))
                                        .build()
                        );
                    }
                }
            } else {
                try {
                    return DocumentLocationResult.builder()
                            .id(documentLocation.getId())
                            .size(-1)
                            .resultType(DownloadResult.INVALID)
                            .build();
                } finally {
                    failureCallback.accept(
                            FailureEntity.builder()
                                    .downloadResult(DownloadResult.INVALID)
                                    .documentLocation(documentLocation)
                                    .build()
                    );
                }
            }
        } catch (Exception e) {
            if (e instanceof StageAccessException stageAccessException) {
                // We rethrow this because we want the app to fail if either the staging or the storage folder is incorrectly
                // configured or unavailable
                throw stageAccessException;
            }

            final DownloadResult downloadResult = documentLocationResultCalculator.transformExceptionToDownloadResult(e);

            try {
                return DocumentLocationResult.builder()
                        .id(documentLocation.getId())
                        .size(-1)
                        .resultType(downloadResult)
                        .build();
            } finally {
                failureCallback.accept(
                        FailureEntity.builder()
                                .downloadResult(downloadResult)
                                .documentLocation(documentLocation)
                                .build()
                );
            }
        }
    }

    @Builder
    public record PersistenceEntity(

            DocumentLocation documentLocation,
            DocumentLocationResult documentLocationResult,
            StageLocation stageLocation,
            Path storagePath
    ) {
    }

    @Builder
    public record FailureEntity(

            DownloadResult downloadResult,
            DocumentLocation documentLocation
    ) {

    }
}
