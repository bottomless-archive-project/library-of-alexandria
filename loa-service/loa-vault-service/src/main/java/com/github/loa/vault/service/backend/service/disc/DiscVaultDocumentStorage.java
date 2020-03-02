package com.github.loa.vault.service.backend.service.disc;

import com.github.loa.compression.service.provider.CompressionServiceProvider;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.domain.exception.VaultAccessException;
import com.github.loa.vault.service.location.VaultLocationFactory;
import com.github.loa.vault.service.backend.domain.VaultPersistenceException;
import com.github.loa.vault.service.backend.service.VaultDocumentStorage;
import com.github.loa.vault.service.location.VaultLocation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Service
@RequiredArgsConstructor
public class DiscVaultDocumentStorage implements VaultDocumentStorage {

    private final VaultLocationFactory vaultLocationFactory;
    private final CompressionServiceProvider compressionServiceProvider;

    @Override
    public void persistDocument(final DocumentEntity documentEntity, final InputStream documentContents) {
        try (final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity)) {
            persistDocument(documentEntity, documentContents, vaultLocation);
        } catch (final IOException e) {
            throw new VaultAccessException("Unable to move document with id " + documentEntity.getId()
                    + " to the vault!", e);
        }
    }

    public void persistDocument(final DocumentEntity documentEntity, final InputStream documentContents,
            final VaultLocation vaultLocation) {
        if (!documentEntity.isCompressed()) {
            try (final OutputStream outputStream = vaultLocation.destination()) {
                IOUtils.copy(documentContents, outputStream);
            } catch (final IOException e) {
                throw new VaultPersistenceException("Failed to persist entity to the vault.", e);
            }
        } else {
            try (final OutputStream outputStream = compressionServiceProvider
                    .getCompressionService(documentEntity.getCompression()).compress(vaultLocation.destination())) {
                IOUtils.copy(documentContents, outputStream);
            } catch (final IOException e) {
                throw new VaultPersistenceException("Failed to persist entity to the vault.", e);
            }
        }
    }
}
