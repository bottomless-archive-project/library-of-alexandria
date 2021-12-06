package com.github.bottomlessarchive.loa.vault.service.archive;

import com.github.bottomlessarchive.loa.checksum.service.ChecksumProvider;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.vault.service.backend.service.VaultDocumentStorage;
import com.github.bottomlessarchive.loa.vault.service.domain.DocumentArchivingContext;
import com.mongodb.MongoWriteException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

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
     * @return the archived document's representation in the database
     */
    public Mono<DocumentEntity> archiveDocument(final DocumentArchivingContext documentArchivingContext) {
        return documentCreationContextFactory.newContext(documentArchivingContext)
                .flatMap(documentEntityFactory::newDocumentEntity)
                .doOnNext(documentEntity -> vaultDocumentStorage.persistDocument(documentEntity,
                        documentArchivingContext.getContent()))
                .retryWhen(
                        Retry.indefinitely()
                                .filter(throwable -> !isDuplicateIndexError(throwable))
                )
                .onErrorResume(throwable -> handleError(throwable, documentArchivingContext));
    }

    private Mono<DocumentEntity> handleError(final Throwable throwable, final DocumentArchivingContext documentArchivingContext) {
        if (isDuplicateIndexError(throwable)) {
            if (log.isInfoEnabled()) {
                log.info("Document with id {} is a duplicate.", documentArchivingContext.getId());
            }

            return checksumProvider.checksum(documentArchivingContext.getContent())
                    .flatMap(checksum -> getOriginalDocumentEntity(checksum, documentArchivingContext))
                    .doOnNext(originalDocumentEntity -> logAddingSourceLocation(originalDocumentEntity, documentArchivingContext))
                    .flatMap(originalDocumentEntity -> addSourceLocation(originalDocumentEntity, documentArchivingContext))
                    .then(Mono.error(throwable));
        } else {
            log.error("Failed to save document!", throwable);

            return Mono.error(throwable);
        }
    }

    private Mono<DocumentEntity> getOriginalDocumentEntity(final String checksum, final DocumentArchivingContext documentArchivingContext) {
        return documentEntityFactory.getDocumentEntity(checksum, documentArchivingContext.getContentLength(),
                documentArchivingContext.getType().toString());
    }

    private void logAddingSourceLocation(final DocumentEntity originalDocumentEntity,
            final DocumentArchivingContext documentArchivingContext) {
        if (log.isInfoEnabled()) {
            log.info("Adding new source location: {} to document: {}.", documentArchivingContext.getSourceLocationId(),
                    originalDocumentEntity.getId());
        }
    }

    private Mono<Void> addSourceLocation(final DocumentEntity originalDocumentEntity,
            final DocumentArchivingContext documentArchivingContext) {
        return documentEntityFactory.addSourceLocation(originalDocumentEntity.getId(), documentArchivingContext.getSourceLocationId());
    }

    private boolean isDuplicateIndexError(final Throwable throwable) {
        return throwable instanceof MongoWriteException mongoWriteException
                && mongoWriteException.getError().getCode() == DUPLICATE_DOCUMENT_ID_ERROR_CODE;
    }
}
