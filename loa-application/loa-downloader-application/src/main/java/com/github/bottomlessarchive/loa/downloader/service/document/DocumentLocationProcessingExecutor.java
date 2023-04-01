package com.github.bottomlessarchive.loa.downloader.service.document;

import com.github.bottomlessarchive.loa.checksum.service.ChecksumProvider;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.downloader.service.document.domain.DocumentArchivingContext;
import com.github.bottomlessarchive.loa.file.FileManipulatorService;
import com.github.bottomlessarchive.loa.io.service.collector.DocumentCollector;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocationResultType;
import com.github.bottomlessarchive.loa.location.service.DocumentLocationManipulator;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import com.github.bottomlessarchive.loa.validator.service.DocumentFileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentLocationProcessingExecutor {

    private final DocumentFileValidator documentFileValidator;
    private final DocumentCollector documentCollector;
    private final DocumentArchiver documentArchiver;
    private final ChecksumProvider checksumProvider;
    private final DocumentEntityFactory documentEntityFactory;
    private final FileManipulatorService fileManipulatorService;
    private final StageLocationFactory stageLocationFactory;
    private final DocumentIdFactory documentIdFactory;
    private final DocumentLocationManipulator documentLocationManipulator;

    public void executeProcessing(final DocumentLocation documentLocation) {
        log.debug("Starting to download document {}.", documentLocation.getLocation());

        final UUID documentId = documentIdFactory.newDocumentId();

        try (StageLocation stageLocation = stageLocationFactory.getLocation(documentId)) {
            final DocumentLocationResultType documentLocationResultType = DocumentLocationResultType.valueOf(
                    documentCollector.acquireDocument(documentLocation.getLocation(), stageLocation.getPath(),
                            documentLocation.getType()).name());

            documentLocationManipulator.updateDownloadResultCode(documentLocation.getId(), documentLocationResultType);

            final String checksum = checksumProvider.checksum(fileManipulatorService.getInputStream(stageLocation.getPath()));
            final long contentLength = fileManipulatorService.size(stageLocation.getPath());

            final Optional<DocumentEntity> documentEntityOptional = documentEntityFactory.getDocumentEntity(
                    checksum, contentLength, documentLocation.getType().toString());
            if (documentEntityOptional.isPresent()) {
                log.info("Document with id: {} is a duplicate.", documentId);

                documentEntityFactory.addSourceLocation(documentEntityOptional.get().getId(), documentLocation.getId());

                return;
            }

            if (documentFileValidator.isValidDocument(documentId, stageLocation, documentLocation.getType())) {
                final DocumentArchivingContext documentArchivingContext = DocumentArchivingContext.builder()
                        .id(documentId)
                        .type(documentLocation.getType())
                        .source(documentLocation.getSourceName())
                        .sourceLocationId(documentLocation.getId())
                        .contents(stageLocation.getPath())
                        .build();

                documentArchiver.archiveDocument(documentArchivingContext);
            } else {
                log.info("Invalid document!");

                if (documentLocationResultType.equals(DocumentLocationResultType.UNKNOWN)) {
                    documentLocationManipulator.updateDownloadResultCode(documentLocation.getId(), DocumentLocationResultType.INVALID);
                }
            }
        } catch (final Exception e) {
            log.info("Error downloading a document: {}!", e.getMessage());
        }
    }
}
