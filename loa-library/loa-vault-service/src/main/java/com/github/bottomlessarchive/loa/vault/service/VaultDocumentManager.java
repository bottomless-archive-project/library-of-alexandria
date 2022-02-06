package com.github.bottomlessarchive.loa.vault.service;

import com.github.bottomlessarchive.loa.compression.service.provider.CompressionServiceProvider;
import com.github.bottomlessarchive.loa.document.service.DocumentManipulator;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.vault.service.archive.ArchivingService;
import com.github.bottomlessarchive.loa.vault.service.domain.DocumentArchivingContext;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocation;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocationFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * Provide access to the content of the documents in the vault.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VaultDocumentManager {

    private final ArchivingService archivingService;
    private final DocumentManipulator documentManipulator;
    private final VaultLocationFactory vaultLocationFactory;
    private final CompressionServiceProvider compressionServiceProvider;

    /**
     * Archive the document provided in the context.
     */
    public void archiveDocument(final DocumentArchivingContext documentArchivingContext) {
        if (log.isInfoEnabled()) {
            log.info("Archiving document with id: {}.", documentArchivingContext.getId());
        }

        archivingService.archiveDocument(documentArchivingContext);
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
        try {
            final InputStream documentContentsInputStream = vaultLocation.download();

            if (documentEntity.isCompressed()) {
                final InputStream decompressedInputStream = compressionServiceProvider.getCompressionService(
                        documentEntity.getCompression()).decompress(documentContentsInputStream);

                return new InputStreamResource(decompressedInputStream);
            } else {
                return new InputStreamResource(documentContentsInputStream);
            }
        } catch (final Exception error) {
            if (error.getMessage().contains("Unable to get document content on a vault location!")
                    || error.getMessage().contains("Error while decompressing document!")) {
                documentManipulator.markCorrupt(documentEntity.getId());
            }

            throw error;
        }
    }

    public boolean documentExists(final DocumentEntity documentEntity) {
        return vaultLocationFactory.getLocation(documentEntity).populated();
    }

    /**
     * Remove the content of a document from the vault.
     *
     * @param documentEntity the document to remove
     */
    public void removeDocument(final DocumentEntity documentEntity) {
        final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity);

        if (vaultLocation.populated()) {
            vaultLocation.clear();
        }
    }

    /**
     * Return the available free space in the vault in bytes.
     *
     * @return the free bytes available
     */
    public long getAvailableSpace() {
        return vaultLocationFactory.getAvailableSpace();
    }
}
