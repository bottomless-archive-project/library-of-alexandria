package com.github.loa.vault.service;

import com.github.loa.compression.service.provider.CompressionServiceProvider;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.service.archive.ArchivingService;
import com.github.loa.vault.service.domain.DocumentArchivingContext;
import com.github.loa.vault.service.location.VaultLocation;
import com.github.loa.vault.service.location.VaultLocationFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.InputStream;

/**
 * Provide access to the content of the documents in the vault.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VaultDocumentManager {

    private final ArchivingService archivingService;
    private final VaultLocationFactory vaultLocationFactory;
    private final CompressionServiceProvider compressionServiceProvider;

    /**
     * Archive the document provided in the context.
     */
    public Mono<DocumentEntity> archiveDocument(final DocumentArchivingContext documentArchivingContext) {
        log.info("Archiving document with id: {}.", documentArchivingContext.getId());

        return Mono.just(documentArchivingContext)
                .flatMap(archivingService::archiveDocument)
                .onErrorResume(error -> Mono.empty());
    }

    /**
     * Return the content of a document as a {@link Resource}.
     *
     * @param documentEntity the document to return the content for
     * @return the content of the document
     */
    public Resource readDocument(final DocumentEntity documentEntity) {
        final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity);

        // The non-compressed entries will be served via a zero-copy response
        // See: https://developer.ibm.com/articles/j-zerocopy/
        final InputStream documentContentsInputStream = vaultLocation.download();

        if (documentEntity.isCompressed()) {
            final InputStream decompressedInputStream = compressionServiceProvider.getCompressionService(
                    documentEntity.getCompression()).decompress(documentContentsInputStream);

            return new InputStreamResource(decompressedInputStream);
        } else {
            return new InputStreamResource(documentContentsInputStream);
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
