package com.github.loa.vault.service;

import com.github.loa.compression.service.provider.CompressionServiceProvider;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.vault.service.backend.service.VaultDocumentStorage;
import com.github.loa.vault.service.domain.DocumentArchivingContext;
import com.github.loa.vault.service.location.VaultLocation;
import com.mongodb.MongoWriteException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Provide access to the content of the documents in the vault.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VaultDocumentManager {

    private final ResourceLoader resourceLoader;
    private final VaultLocationFactory vaultLocationFactory;
    private final CompressionServiceProvider compressionServiceProvider;
    private final DocumentEntityFactory documentEntityFactory;
    private final DocumentCreationContextFactory documentCreationContextFactory;
    private final VaultDocumentStorage vaultDocumentStorage;

    /**
     * Archive the document provided in the context.
     */
    public Mono<DocumentEntity> archiveDocument(final DocumentArchivingContext documentArchivingContext) {
        log.info("Archiving document with id: {}.", documentArchivingContext.getId());

        return Mono.just(documentArchivingContext)
                .flatMap(archivingContext -> documentCreationContextFactory.newContext(archivingContext)
                        .flatMap(documentEntityFactory::newDocumentEntity)
                        .doOnNext(documentEntity -> vaultDocumentStorage.persistDocument(documentEntity,
                                new ByteArrayInputStream(documentArchivingContext.getContent())))
                        .doOnError(throwable -> handleError(throwable, documentArchivingContext))
                        .retry(throwable -> !isDuplicateIndexError(throwable))
                )
                .onErrorResume(error -> Mono.empty());
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

    /**
     * Return the content of a document as an {@link InputStream}.
     *
     * @param documentEntity the document to return the content for
     * @return the content of the document
     */
    public Resource readDocument(final DocumentEntity documentEntity) {
        final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity);

        // The non-compressed entries will be served via a zero-copy response
        // See: https://developer.ibm.com/articles/j-zerocopy/
        if (documentEntity.isCompressed()) {
            final InputStream decompressedInputStream = compressionServiceProvider
                    .getCompressionService(documentEntity.getCompression()).decompress(vaultLocation.content());

            return new InputStreamResource(decompressedInputStream);
        } else {
            return resourceLoader.getResource("file:/" + vaultLocation.file().getPath());
        }
    }

    /**
     * Remove the content of a document from the vault.
     *
     * @param documentEntity the document to remove
     * @return the document that was removed
     */
    public Mono<DocumentEntity> removeDocument(final DocumentEntity documentEntity) {
        return Mono.just(documentEntity)
                .map(vaultLocationFactory::getLocation)
                .doOnNext(VaultLocation::clear)
                .thenReturn(documentEntity);
    }
}
