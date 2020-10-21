package com.github.loa.vault.service.location.file.domain;

import com.github.loa.vault.domain.exception.StorageAccessException;
import com.github.loa.vault.service.location.VaultLocation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A {@link VaultLocation} implementation that stores the document contents on a local drive.
 */
@RequiredArgsConstructor
public class FileVaultLocation implements VaultLocation {

    private final Path vaultLocation;

    /**
     * {@inheritDoc}
     */
    @Override
    public void upload(final byte[] documentContents) {
        try (OutputStream outputStream = Files.newOutputStream(vaultLocation)) {
            IOUtils.copy(new ByteArrayInputStream(documentContents), outputStream);
        } catch (final IOException e) {
            throw new StorageAccessException("Unable to create file in vault!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream download() {
        try {
            return Files.newInputStream(vaultLocation);
        } catch (final IOException e) {
            throw new StorageAccessException("Unable to get document content on a vault location!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        try {
            Files.delete(vaultLocation);
        } catch (final IOException e) {
            throw new StorageAccessException("Unable to delete document contents on vault location!", e);
        }
    }
}
