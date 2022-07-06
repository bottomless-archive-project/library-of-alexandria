package com.github.bottomlessarchive.loa.vault.service.archive;

import com.github.bottomlessarchive.loa.document.service.DocumentManipulator;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.bottomlessarchive.loa.staging.service.client.StagingClient;
import com.github.bottomlessarchive.loa.vault.service.VaultDocumentManager;
import com.github.bottomlessarchive.loa.vault.service.domain.DocumentArchivingContext;
import com.mongodb.MongoWriteException;
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

    private static final int DUPLICATE_DOCUMENT_ID_ERROR_CODE = 11000;

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
                final DocumentEntity documentEntity = documentEntityFactory.newDocumentEntity(documentCreationContext);

                try (InputStream documentContent = stagingClient.grabFromStaging(documentArchivingContext.getId())) {
                    vaultDocumentManager.archiveDocument(documentEntity, documentArchivingContext, documentContent);

                    documentManipulator.markDownloaded(documentEntity.getId());

                    // The document was successfully saved
                    return;
                } catch (IOException e) {
                    log.error("Failed to download the document's contents!", e);
                }
            } catch (final Exception e) {
                if (isDuplicateIndexError(e)) {
                    handleDuplicate(documentArchivingContext);
                    //TODO: Delete it from stage

                    // It is a duplicate
                    return;
                }

                // We were unable to save it! Needs to retry!
                log.error("Failed to save document!", e);
            }
        }
    }

    private void handleDuplicate(final DocumentArchivingContext documentArchivingContext) {
        log.info("Document with id {} is a duplicate.", documentArchivingContext.getId());

        getOriginalDocumentEntity(documentArchivingContext.getChecksum(), documentArchivingContext)
                .ifPresent(originalDocumentEntity -> {
                    logAddingSourceLocation(originalDocumentEntity, documentArchivingContext);
                    addSourceLocation(originalDocumentEntity, documentArchivingContext);
                });
    }

    private Optional<DocumentEntity> getOriginalDocumentEntity(final String checksum,
            final DocumentArchivingContext documentArchivingContext) {
        return documentEntityFactory.getDocumentEntity(checksum, documentArchivingContext.getOriginalContentLength(),
                documentArchivingContext.getType().toString());
    }

    private void logAddingSourceLocation(final DocumentEntity originalDocumentEntity,
            final DocumentArchivingContext documentArchivingContext) {

        if (documentArchivingContext.getSourceLocationId().isPresent()) {
            if (log.isInfoEnabled()) {
                log.info("Adding new source location: {} to document: {}.", documentArchivingContext.getSourceLocationId().get(),
                        originalDocumentEntity.getId());
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info("Doesn't add a new source location to document: {} because it is null.", originalDocumentEntity.getId());
            }
        }
    }

    private void addSourceLocation(final DocumentEntity originalDocumentEntity, final DocumentArchivingContext documentArchivingContext) {
        documentArchivingContext.getSourceLocationId().ifPresent(sourceLocationId ->
                documentEntityFactory.addSourceLocation(originalDocumentEntity.getId(), sourceLocationId));
    }

    private boolean isDuplicateIndexError(final Object throwable) {
        return throwable instanceof MongoWriteException mongoWriteException
                && mongoWriteException.getError().getCode() == DUPLICATE_DOCUMENT_ID_ERROR_CODE;
    }
}
