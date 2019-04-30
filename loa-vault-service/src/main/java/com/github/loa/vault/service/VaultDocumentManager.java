package com.github.loa.vault.service;

import com.github.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.compression.service.CompressionServiceProvider;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.domain.exception.VaultAccessException;
import com.github.loa.vault.service.location.domain.VaultLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class VaultDocumentManager {

    private final VaultLocationFactory vaultLocationFactory;
    private final CompressionServiceProvider compressionServiceProvider;
    private final CompressionConfigurationProperties compressionConfigurationProperties;

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
        return readDocument(documentEntity.getId(), documentEntity.getCompression());
    }

    public byte[] readDocument(final String documentId, final DocumentCompression compression) {
        try (final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentId, compression)) {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            compressionServiceProvider.getCompressionService(compression)
                    .decompress(vaultLocation.content(), outputStream);

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new VaultAccessException("Unable to read document with id " + documentId + " from the vault!", e);
        }
    }

    public void removeDocument(final DocumentEntity documentEntity) {
        final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity);

        vaultLocation.clear();
    }
}
