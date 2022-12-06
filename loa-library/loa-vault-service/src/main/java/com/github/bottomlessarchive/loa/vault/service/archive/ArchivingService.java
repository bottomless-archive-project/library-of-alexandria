package com.github.bottomlessarchive.loa.vault.service.archive;

import com.github.bottomlessarchive.loa.document.service.DocumentManipulator;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.domain.DuplicateDocumentException;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.bottomlessarchive.loa.staging.service.client.StagingClient;
import com.github.bottomlessarchive.loa.vault.service.VaultDocumentManager;
import com.github.bottomlessarchive.loa.vault.service.domain.DocumentArchivingContext;
import com.github.bottomlessarchive.loa.number.service.domain.HexConversionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchivingService {

    private final StagingClient stagingClient;
    private final DocumentManipulator documentManipulator;
    private final VaultDocumentManager vaultDocumentManager;
    private final DocumentEntityFactory documentEntityFactory;
    private final DocumentCreationContextFactory documentCreationContextFactory;

    public void archiveDocument(final DocumentArchivingContext documentArchivingContext) {
        final DocumentCreationContext documentCreationContext = documentCreationContextFactory.newContext(
                documentArchivingContext);

        // Retry until we eventually succeed to save the document
        while (true) {
            try {
                final DocumentEntity documentEntity = documentArchivingContext.fromBeacon()
                        ? documentEntityFactory.getDocumentEntity(documentArchivingContext.id()).orElseThrow()
                        : documentEntityFactory.newDocumentEntity(documentCreationContext);

                if (documentArchivingContext.fromBeacon()) {
                    documentManipulator.updateCompression(documentArchivingContext.id(), documentArchivingContext.compression());
                }

                try (InputStream documentContent = stagingClient.grabFromStaging(documentArchivingContext.id())) {
                    vaultDocumentManager.archiveDocument(documentEntity, documentArchivingContext, documentContent);

                    documentManipulator.markDownloaded(documentEntity.getId());

                    // The document was successfully saved
                    return;
                } catch (IOException e) {
                    log.error("Failed to download the document's contents!", e);
                }
            } catch (final DuplicateDocumentException duplicateDocumentException) {
                handleDuplicate(documentArchivingContext);

                stagingClient.deleteFromStaging(documentArchivingContext.id());

                // It is a duplicate
                return;
            } catch (HexConversionException e) {
                // This should never happen under normal circumstances
                throw new IllegalStateException("Hex conversion failed! This should never happen under normal circumstances,"
                        + " please report it to the developers as soon as possible!", e);
            } catch (final Exception e) {
                // We were unable to save it! Needs to retry!
                log.error("Failed to save document!", e);
            }
        }
    }

    private void handleDuplicate(final DocumentArchivingContext documentArchivingContext) {
        log.info("Document with id {} is a duplicate.", documentArchivingContext.id());

        getOriginalDocumentEntity(documentArchivingContext.checksum(), documentArchivingContext)
                .ifPresent(originalDocumentEntity -> {
                    logAddingSourceLocation(originalDocumentEntity, documentArchivingContext);
                    addSourceLocation(originalDocumentEntity, documentArchivingContext);
                });
    }

    private Optional<DocumentEntity> getOriginalDocumentEntity(final String checksum,
            final DocumentArchivingContext documentArchivingContext) {
        return documentEntityFactory.getDocumentEntity(checksum, documentArchivingContext.originalContentLength(),
                documentArchivingContext.type().toString());
    }

    private void logAddingSourceLocation(final DocumentEntity originalDocumentEntity,
            final DocumentArchivingContext documentArchivingContext) {

        if (log.isInfoEnabled()) {
            documentArchivingContext.sourceLocationId()
                    .ifPresentOrElse(
                            sourceLocationId -> log.info("Adding new source location: {} to document: {}.", sourceLocationId,
                                    originalDocumentEntity.getId()),
                            () -> log.info("Doesn't add a new source location to document: {} because it is null.",
                                    originalDocumentEntity.getId())
                    );
        }
    }

    private void addSourceLocation(final DocumentEntity originalDocumentEntity, final DocumentArchivingContext documentArchivingContext) {
        documentArchivingContext.sourceLocationId()
                .ifPresent(sourceLocationId -> documentEntityFactory.addSourceLocation(originalDocumentEntity.getId(), sourceLocationId));
    }
}
