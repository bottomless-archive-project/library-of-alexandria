package com.github.loa.vault.service;

import com.github.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.compression.service.provider.CompressionServiceProvider;
import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.domain.exception.VaultAccessException;
import com.github.loa.vault.service.location.domain.VaultLocation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provide access to the content of the documents in the vault.
 */
@Service
@RequiredArgsConstructor
public class VaultDocumentManager {

    private final ResourceLoader resourceLoader;
    private final VaultLocationFactory vaultLocationFactory;
    private final CompressionServiceProvider compressionServiceProvider;
    private final CompressionConfigurationProperties compressionConfigurationProperties;
    private final DocumentManipulator documentManipulator;

    /**
     * Archive the content of an input stream as the content of the provided document in the vault.
     *
     * @param documentEntity   the document to save the content for
     * @param documentContents the content to archive for the document
     */
    public void archiveDocument(final DocumentEntity documentEntity, final Resource documentContents) {
        try {
            archiveDocument(documentEntity, documentContents.getInputStream(),
                    compressionConfigurationProperties.getAlgorithm());
        } catch (IOException e) {
            throw new RuntimeException("Unable to read document content!");
        }
    }

    /**
     * Archive the content of an input stream as the content of the provided document in the vault.
     *
     * @param documentEntity   the document to save the content for
     * @param documentContents the content to archive for the document
     */
    public void archiveDocument(final DocumentEntity documentEntity, final InputStream documentContents,
            final DocumentCompression documentCompression) {
        try (final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity)) {
            if (documentEntity.isCompressed()) {
                try (final OutputStream outputStream = compressionServiceProvider
                        .getCompressionService(documentCompression).compress(vaultLocation.destination())) {
                    IOUtils.copy(documentContents, outputStream);
                }
            } else {
                IOUtils.copy(documentContents, vaultLocation.destination());
            }
        } catch (IOException e) {
            throw new VaultAccessException("Unable to move document with id " + documentEntity.getId()
                    + " to the vault!", e);
        }
    }

    /**
     * Return the content of a document as an {@link InputStream}.
     *
     * @param documentEntity the document to return the content for
     * @return the content of the document
     */
    public Resource readDocument(final DocumentEntity documentEntity) {
        final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity,
                documentEntity.getCompression());

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
     */
    public void removeDocument(final DocumentEntity documentEntity) {
        final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity,
                documentEntity.getCompression());

        vaultLocation.clear();

        documentManipulator.markRemoved(documentEntity.getId());
    }
}
