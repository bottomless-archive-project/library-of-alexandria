package com.github.loa.vault.service.location.file.domain;

import com.github.loa.vault.domain.exception.StorageAccessException;
import com.github.loa.vault.service.location.VaultLocation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;

@RequiredArgsConstructor
public class FileVaultLocation implements VaultLocation {

    //TODO: This should be a path?
    private final File vaultLocation;

    /**
     * Return an output stream that points to the space where the document's content are archived. Should be used if
     * you want to modify the content of the document.
     *
     * @throws StorageAccessException when unable to create the destination file
     */
    @Override
    public void upload(final byte[] documentContents) {
        try (OutputStream outputStream = Files.newOutputStream(vaultLocation.toPath())) {
            IOUtils.copy(new ByteArrayInputStream(documentContents), outputStream);
        } catch (final IOException e) {
            throw new StorageAccessException("Unable to create file in vault!", e);
        }
    }

    /**
     * Return the content of the file in the vault.
     *
     * @return the content of the file
     */
    @Override
    public InputStream download() {
        try {
            return Files.newInputStream(vaultLocation.toPath());
        } catch (final IOException e) {
            throw new StorageAccessException("Unable to get the content of a vault location!", e);
        }
    }

    @Override
    public void clear() {
        vaultLocation.delete();
    }
}
