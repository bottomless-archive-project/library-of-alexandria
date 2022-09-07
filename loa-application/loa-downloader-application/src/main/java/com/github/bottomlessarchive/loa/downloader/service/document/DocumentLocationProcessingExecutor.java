package com.github.bottomlessarchive.loa.downloader.service.document;

import com.github.bottomlessarchive.loa.checksum.service.ChecksumProvider;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.downloader.service.document.domain.DocumentArchivingContext;
import com.github.bottomlessarchive.loa.file.FileManipulatorService;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocationResultType;
import com.github.bottomlessarchive.loa.location.service.DocumentLocationManipulator;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.bottomlessarchive.loa.url.service.collector.FileCollector;
import com.github.bottomlessarchive.loa.validator.service.DocumentFileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentLocationProcessingExecutor {

    private final DocumentFileValidator documentFileValidator;
    private final FileCollector fileCollector;
    private final DocumentArchiver documentArchiver;
    private final ChecksumProvider checksumProvider;
    private final DocumentEntityFactory documentEntityFactory;
    private final FileManipulatorService fileManipulatorService;
    private final StageLocationFactory stageLocationFactory;
    private final DocumentLocationManipulator documentLocationManipulator;

    public void executeProcessing(final String documentLocationId, final String documentLocationSource, final URL documentLocationURL,
            final DocumentType documentType) {
        log.debug("Starting to download document {}.", documentLocationURL);

        final UUID documentId = UUID.randomUUID();

        final StageLocation stageLocation = stageLocationFactory.getLocation(documentId.toString(), documentType);

        try {
            final DocumentLocationResultType documentLocationResultType = DocumentLocationResultType.valueOf(
                    fileCollector.acquireFile(documentLocationURL, stageLocation.getPath(), documentType).name());

            documentLocationManipulator.updateDownloadResultCode(documentLocationId, documentLocationResultType);

            final String checksum = checksumProvider.checksum(fileManipulatorService.getInputStream(stageLocation.getPath()));
            final long contentLength = fileManipulatorService.size(stageLocation.getPath());

            final Optional<DocumentEntity> documentEntityOptional = documentEntityFactory.getDocumentEntity(
                    checksum, contentLength, documentType.toString());
            if (documentEntityOptional.isPresent()) {
                log.info("Document with id: {} is a duplicate.", documentId);

                documentEntityFactory.addSourceLocation(documentEntityOptional.get().getId(), documentLocationId);

                return;
            }

            if (documentFileValidator.isValidDocument(documentId.toString(), documentType)) {
                final DocumentArchivingContext documentArchivingContext = DocumentArchivingContext.builder()
                        .id(documentId)
                        .type(documentType)
                        .source(documentLocationSource)
                        .sourceLocationId(documentLocationId)
                        .contents(stageLocation.getPath())
                        .build();

                documentArchiver.archiveDocument(documentArchivingContext);
            } else {
                log.info("Invalid document!");
            }
        } catch (final Exception e) {
            log.info("Error downloading a document: {}!", e.getMessage());
        } finally {
            if (stageLocation.exists()) {
                stageLocation.cleanup();
            }
        }
    }
}
