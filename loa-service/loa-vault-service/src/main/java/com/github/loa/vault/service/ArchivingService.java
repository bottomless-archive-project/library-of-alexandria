package com.github.loa.vault.service;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.vault.service.backend.service.VaultDocumentStorage;
import com.github.loa.vault.service.domain.DocumentArchivingContext;
import com.mongodb.MongoWriteException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchivingService {

    private final DocumentEntityFactory documentEntityFactory;
    private final DocumentCreationContextFactory documentCreationContextFactory;
    private final VaultDocumentStorage vaultDocumentStorage;

    public Mono<DocumentEntity> archiveDocument(final DocumentArchivingContext documentArchivingContext) {
        return documentCreationContextFactory.newContext(documentArchivingContext)
                .flatMap(documentEntityFactory::newDocumentEntity)
                .doOnNext(documentEntity -> vaultDocumentStorage.persistDocument(documentEntity,
                        new ByteArrayInputStream(documentArchivingContext.getContent())))
                .doOnError(throwable -> handleError(throwable, documentArchivingContext))
                .retry(throwable -> !isDuplicateIndexError(throwable));
    }

    private void handleError(final Throwable throwable, final DocumentArchivingContext documentArchivingContext) {
        if (isDuplicateIndexError(throwable)) {
            log.info("Document with id {} is a duplicate.", documentArchivingContext.getId());
        } else {
            log.error("Failed to save document!", throwable);
        }
    }

    private boolean isDuplicateIndexError(final Throwable throwable) {
        return throwable instanceof MongoWriteException
                && throwable.getMessage().startsWith("E11000 duplicate key error");
    }
}
