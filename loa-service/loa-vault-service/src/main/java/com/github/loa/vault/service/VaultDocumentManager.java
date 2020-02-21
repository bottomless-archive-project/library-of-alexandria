package com.github.loa.vault.service;

import com.github.loa.checksum.service.ChecksumProvider;
import com.github.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.loa.compression.service.provider.CompressionServiceProvider;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.loa.vault.configuration.VaultConfigurationProperties;
import com.github.loa.vault.domain.exception.VaultAccessException;
import com.github.loa.vault.service.domain.DocumentArchivingContext;
import com.github.loa.vault.service.location.VaultLocation;
import com.mongodb.MongoWaitQueueFullException;
import com.mongodb.MongoWriteException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

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
    private final CompressionConfigurationProperties compressionConfigurationProperties;
    private final VaultConfigurationProperties vaultConfigurationProperties;
    private final DocumentEntityFactory documentEntityFactory;
    private final ChecksumProvider checksumProvider;

    /**
     * Archive the content of an input stream as the content of the provided document in the vault.
     */
    public Mono<DocumentEntity> archiveDocument(final DocumentArchivingContext documentArchivingContext) {
        log.info("Archiving document with id: {}.", documentArchivingContext.getId());

        return Mono.just(documentArchivingContext)
                .flatMap(stageLocation -> checksumProvider.checksum(documentArchivingContext.getContent())
                        .flatMap(checksum -> documentEntityFactory.newDocumentEntity(
                                DocumentCreationContext.builder()
                                        .id(documentArchivingContext.getId())
                                        .type(documentArchivingContext.getType())
                                        .status(DocumentStatus.DOWNLOADED)
                                        .source(documentArchivingContext.getSource())
                                        .versionNumber(vaultConfigurationProperties.getVersionNumber())
                                        .compression(compressionConfigurationProperties.getAlgorithm())
                                        .checksum(checksum)
                                        .fileSize(documentArchivingContext.getContentLength())
                                        .build()
                                )
                        )
                        .flatMap(documentEntity -> Mono.fromSupplier(
                                () -> saveDocument(documentEntity, documentArchivingContext.getContent())))
                        .doOnError(throwable -> {
                            // Ignoring MongoWaitQueueFullException. It will be retried.
                            if ((throwable.getCause() instanceof MongoWaitQueueFullException)) {
                                return;
                            }

                            if (isDuplicateIndexError(throwable)) {
                                log.info("Document with id {} is a duplicate.", documentArchivingContext.getId());
                            } else {
                                log.error("Failed to save document!", throwable);
                            }
                        })
                        .retry(throwable -> !isDuplicateIndexError(throwable))
                )
                .onErrorResume(error -> Mono.empty());
    }

    private boolean isDuplicateIndexError(final Throwable throwable) {
        return throwable instanceof MongoWriteException
                && throwable.getMessage().startsWith("E11000 duplicate key error");
    }

    public DocumentEntity saveDocument(final DocumentEntity documentEntity, final byte[] documentContents) {
        try (final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity);
             final InputStream documentInputStream = new ByteArrayInputStream(documentContents)) {
            saveDocumentContents(documentEntity, documentInputStream, vaultLocation);
        } catch (IOException e) {
            throw new VaultAccessException("Unable to move document with id " + documentEntity.getId()
                    + " to the vault!", e);
        }

        return documentEntity;
    }

    public void saveDocumentContents(final DocumentEntity documentEntity, final InputStream documentContents,
            final VaultLocation vaultLocation) throws IOException {
        if (!documentEntity.isCompressed()) {
            try (final OutputStream outputStream = vaultLocation.destination()) {
                IOUtils.copy(documentContents, outputStream);
            }
        } else {
            try (final OutputStream outputStream = compressionServiceProvider
                    .getCompressionService(documentEntity.getCompression()).compress(vaultLocation.destination())) {
                IOUtils.copy(documentContents, outputStream);
            }
        }
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
