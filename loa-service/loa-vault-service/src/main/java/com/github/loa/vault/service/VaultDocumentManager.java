package com.github.loa.vault.service;

import com.github.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.loa.compression.service.provider.CompressionServiceProvider;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.domain.exception.VaultAccessException;
import com.github.loa.vault.service.location.domain.VaultLocation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provide access to the content of the documents in the vault.
 */
@Service
@RequiredArgsConstructor
public class VaultDocumentManager {

    private final VaultLocationFactory vaultLocationFactory;
    private final CompressionServiceProvider compressionServiceProvider;
    private final CompressionConfigurationProperties compressionConfigurationProperties;

    /**
     * Archive the content of an input stream as the content of the provided document in the vault.
     *
     * @param documentEntity   the document to save the content for
     * @param documentContents the content to archive for the document
     */
    public void archiveDocument(final DocumentEntity documentEntity, final InputStream documentContents) {
        try (final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity)) {
            try (final OutputStream outputStream = compressionServiceProvider.getCompressionService(
                    compressionConfigurationProperties.getAlgorithm()).compress(vaultLocation.destination())) {
                IOUtils.copy(documentContents, outputStream);
            }
        } catch (IOException e) {
            throw new VaultAccessException("Unable to move document with id " + documentEntity.getId()
                    + " to the vault!", e);
        }
    }

    public InputStream readDocument(final DocumentEntity documentEntity, final VaultLocation vaultLocation) {
        return compressionServiceProvider.getCompressionService(documentEntity.getCompression())
                .decompress(vaultLocation.content());
    }

    public void removeDocument(final DocumentEntity documentEntity) {
        final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity,
                documentEntity.getCompression());

        vaultLocation.clear();
    }
}
