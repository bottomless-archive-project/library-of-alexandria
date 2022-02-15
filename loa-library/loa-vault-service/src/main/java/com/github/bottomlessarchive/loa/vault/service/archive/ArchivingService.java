package com.github.bottomlessarchive.loa.vault.service.archive;

import com.github.bottomlessarchive.loa.checksum.service.ChecksumProvider;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.bottomlessarchive.loa.vault.service.backend.service.VaultDocumentStorage;
import com.github.bottomlessarchive.loa.vault.service.domain.DocumentArchivingContext;
import com.mongodb.MongoWriteException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchivingService {

    private static final int DUPLICATE_DOCUMENT_ID_ERROR_CODE = 11000;

    private final ChecksumProvider checksumProvider;
    private final DocumentEntityFactory documentEntityFactory;
    private final DocumentCreationContextFactory documentCreationContextFactory;
    private final VaultDocumentStorage vaultDocumentStorage;

    /**
     * Archives a document to the vault. The document will be saved to the vault's physical storage and inserted into the database as well.
     * If the document is a duplicate (evaluated by its checksum) then the original document's (the one that the newly inserted document is
     * the duplicate of) source locations will be updated with the source location of the document that is being inserted.
     *
     * @param documentArchivingContext the context of the document to archive
     */
    public void archiveDocument(final DocumentArchivingContext documentArchivingContext) {
        final DocumentCreationContext documentCreationContext = documentCreationContextFactory.newContext(
                documentArchivingContext);

        while (true) {
            try {
                final DocumentEntity documentEntity = documentEntityFactory.newDocumentEntity(documentCreationContext);

                vaultDocumentStorage.persistDocument(documentEntity, documentArchivingContext.getContent());

                break;
            } catch (final Exception e) {
                if (isDuplicateIndexError(e)) {
                    handleDuplicate(documentArchivingContext);

                    break;
                }

                log.error("Failed to save document!", e);
            }
        }
    }

    private void handleDuplicate(final DocumentArchivingContext documentArchivingContext) {
        log.info("Document with id {} is a duplicate.", documentArchivingContext.getId());

        final String checksum = checksumProvider.checksum(documentArchivingContext.getContent());

        getOriginalDocumentEntity(checksum, documentArchivingContext)
                .ifPresent(originalDocumentEntity -> {
                    logAddingSourceLocation(originalDocumentEntity, documentArchivingContext);
                    addSourceLocation(originalDocumentEntity, documentArchivingContext);
                });
    }

    private Optional<DocumentEntity> getOriginalDocumentEntity(final String checksum,
            final DocumentArchivingContext documentArchivingContext) {
        return documentEntityFactory.getDocumentEntity(checksum, documentArchivingContext.getContentLength(),
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
