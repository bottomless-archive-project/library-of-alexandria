package com.github.loa.vault.service.location.file.domain;

import com.github.loa.vault.domain.exception.VaultAccessException;
import com.github.loa.vault.service.location.domain.VaultLocation;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@RequiredArgsConstructor
public class FileVaultLocation implements VaultLocation {

    private final File vaultLocation;

    /**
     * Moves a file to the location. This is a move command so the original file instance will be deleted.
     *
     * @param file the file to move
     * @throws VaultAccessException when unable to move the file
     */
    @Override
    public void move(final File file) {
        try {
            Files.move(file.toPath(), vaultLocation.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new VaultAccessException("Unable to move file to vault!", e);
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
}
