package com.github.loa.vault.service;

import com.github.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.loa.compression.service.CompressionServiceProvider;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.domain.exception.VaultAccessException;
import com.github.loa.vault.service.location.domain.VaultLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
        archiveDocument(documentEntity.getId(), documentContents);
    }

    public void archiveDocument(final String documentId, final InputStream documentContents) {
        try (final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentId)) {
            compressionServiceProvider.getCompressionService(compressionConfigurationProperties.getAlgorithm())
                    .compress(documentContents, vaultLocation.destination());
        } catch (IOException e) {
            throw new VaultAccessException("Unable to move document with id " + documentId + " to the vault!", e);
        }
    }

    public byte[] readDocument(final DocumentEntity documentEntity) {
        try (final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity.getId(),
                documentEntity.getCompression())) {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            try (final InputStream vaultLocationContent = vaultLocation.content()) {
                compressionServiceProvider.getCompressionService(documentEntity.getCompression())
                        .decompress(vaultLocationContent, outputStream);
            }

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new VaultAccessException("Unable to read document with id " + documentEntity.getId() + " from the vault!", e);
        }
    }

    public void removeDocument(final DocumentEntity documentEntity) {
        final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity,
                documentEntity.getCompression());

        vaultLocation.clear();
    }
}
