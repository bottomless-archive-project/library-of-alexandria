package com.github.loa.vault.service.location.domain;

import java.io.File;

public interface VaultLocation {

    /**
     * Moves a file to the location. This is a move command so the original file instance will be deleted.
     *
     * @param file the file to move
     */
    void move(final File file);

    void move(final VaultLocation newLocation);

    /**
     * Get the content of the location as a byte array.
     *
     * @return the content of the location
     */
    byte[] getContent();
}
