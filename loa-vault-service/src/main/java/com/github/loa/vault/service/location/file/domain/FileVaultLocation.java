package com.github.loa.vault.service.location.file.domain;

import com.github.loa.vault.domain.exception.VaultAccessException;
import com.github.loa.vault.service.location.domain.VaultLocation;
import lombok.RequiredArgsConstructor;

import java.io.*;
import java.nio.file.Files;

@RequiredArgsConstructor
public class FileVaultLocation implements VaultLocation {

    private final File vaultLocation;

    /**
     * Return an output stream that points to the space where the document's content are archived. Should be used if
     * you want to modify the content of the document.
     *
     * @return the location for the document's content
     * @throws VaultAccessException when unable to create the destination file
     */
    @Override
    public OutputStream destination() {
        try {
            return new FileOutputStream(vaultLocation);
        } catch (FileNotFoundException e) {
            throw new VaultAccessException("Unable to create file in vault!", e);
        }
    }

    /**
     * Return the content of the file in the vault.
     *
     * @return the content of the file
     */
    @Override
    public byte[] getContent() {
        try {
            return Files.readAllBytes(vaultLocation.toPath());
        } catch (IOException e) {
            throw new VaultAccessException("Unable to get getContent of vault location!", e);
        }
    }

    @Override
    public void close() {
    }
}
