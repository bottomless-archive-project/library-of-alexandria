package com.github.bottomlessarchive.loa.vault.service.location.file.domain;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.vault.domain.exception.StorageAccessException;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocation;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * A {@link VaultLocation} implementation that stores the document contents on a local drive.
 */
@ToString
@RequiredArgsConstructor
public class FileVaultLocation implements VaultLocation {

    private final Path vaultLocation;
    private final DocumentCompression documentCompression;

    /**
     * {@inheritDoc}
     */
    @Override
    public void upload(final InputStream documentContents, final long contentLength) {
        try (OutputStream outputStream = Files.newOutputStream(vaultLocation)) {
            IOUtils.copy(documentContents, outputStream);
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
    public boolean populated() {
        return Files.exists(vaultLocation);
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

    @Override
    public Optional<DocumentCompression> getCompression() {
        return Optional.ofNullable(documentCompression);
    }
}
