package com.github.bottomlessarchive.loa.downloader.service.document;

import com.github.bottomlessarchive.loa.checksum.service.ChecksumProvider;
import com.github.bottomlessarchive.loa.document.service.DocumentTypeCalculator;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.downloader.service.document.domain.DocumentArchivingContext;
import com.github.bottomlessarchive.loa.downloader.service.file.FileCollector;
import com.github.bottomlessarchive.loa.file.FileManipulatorService;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
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
public class DocumentLocationProcessor {

    private final DocumentFileValidator documentFileValidator;
    private final FileCollector fileCollector;
    private final DocumentArchiver documentArchiver;
    private final ChecksumProvider checksumProvider;
    private final DocumentTypeCalculator documentTypeCalculator;
    private final DocumentEntityFactory documentEntityFactory;
    private final FileManipulatorService fileManipulatorService;
    private final StageLocationFactory stageLocationFactory;

    public void doProcessDocumentLocation(final DocumentLocation documentLocation) {
        final URL documentLocationURL = documentLocation.getLocation().toUrl().orElseThrow();
        final Optional<DocumentType> documentTypeOptional = documentTypeCalculator.calculate(documentLocationURL);

        if (documentTypeOptional.isEmpty()) {
            log.debug("Document on location {} has an unknown document type!", documentLocation);

            return;
        }

        final DocumentType documentType = documentTypeOptional.get();

        log.debug("Starting to download document {}.", documentLocationURL);

        final UUID documentId = UUID.randomUUID();

        final StageLocation stageLocation = stageLocationFactory.getLocation(documentId.toString(), documentType);

        try {
            fileCollector.acquireFile(documentLocation.getId(), documentLocationURL, stageLocation.getPath(), documentType);

            final String checksum = checksumProvider.checksum(fileManipulatorService.getInputStream(stageLocation.getPath()));
            final long contentLength = fileManipulatorService.size(stageLocation.getPath());

            final Optional<DocumentEntity> documentEntityOptional = documentEntityFactory.getDocumentEntity(
                    checksum, contentLength, documentType.toString());
            if (documentEntityOptional.isPresent()) {
                log.info("Document with id: {} is a duplicate.", documentId);

                documentEntityFactory.addSourceLocation(documentEntityOptional.get().getId(), documentLocation.getId());

                return;
            }

            if (documentFileValidator.isValidDocument(documentId.toString(), documentType)) {
                final DocumentArchivingContext documentArchivingContext = DocumentArchivingContext.builder()
                        .id(documentId)
                        .type(documentType)
                        .source(documentLocation.getSourceName())
                        .sourceLocationId(documentLocation.getId())
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
