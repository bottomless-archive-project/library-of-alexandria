package com.github.loa.vault.service.location.file.domain;

import com.github.loa.vault.domain.exception.VaultAccessException;
import com.github.loa.vault.service.location.VaultLocation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.*;

@RequiredArgsConstructor
public class FileVaultLocation implements VaultLocation {

    private final File vaultLocation;

    /**
     * Return an output stream that points to the space where the document's content are archived. Should be used if
     * you want to modify the content of the document.
     *
     * @throws VaultAccessException when unable to create the destination file
     */
    @Override
    public void upload(final byte[] documentContents) {
        try (final OutputStream outputStream = new FileOutputStream(vaultLocation)) {
            IOUtils.copy(new ByteArrayInputStream(documentContents), outputStream);
        } catch (final IOException e) {
            throw new VaultAccessException("Unable to create file in vault!", e);
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
            return new FileInputStream(vaultLocation);
        } catch (final IOException e) {
            throw new VaultAccessException("Unable to get the content of a vault location!", e);
        }
    }

    @Override
    public void clear() {
        vaultLocation.delete();
    }

    /**
     * Closes the vault location, free up every resource that was used to access it.
     */
    @Override
    public void close() {
        // Nothing to close in the file based vault.
    }
}
