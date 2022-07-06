package com.github.bottomlessarchive.loa.vault.service;

import com.github.bottomlessarchive.loa.compression.service.compressor.provider.CompressorServiceProvider;
import com.github.bottomlessarchive.loa.document.service.DocumentManipulator;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.vault.service.backend.service.VaultDocumentStorage;
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

    private final DocumentManipulator documentManipulator;
    private final VaultLocationFactory vaultLocationFactory;
    private final CompressorServiceProvider compressorServiceProvider;
    private final VaultDocumentStorage vaultDocumentStorage;

    /**
     * Archives a document to the vault. The document will be saved to the vault's physical storage.
     *
     * @param documentArchivingContext the context of the document to archive
     */
    public void archiveDocument(final DocumentEntity documentEntity, final DocumentArchivingContext documentArchivingContext,
            final InputStream documentContent) {
        log.info("Archiving document with id: {}.", documentArchivingContext.getId());

        vaultDocumentStorage.persistDocument(documentEntity, documentContent, documentArchivingContext.getContentLength());
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
                final InputStream decompressedInputStream = compressorServiceProvider.getCompressionService(
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
}
