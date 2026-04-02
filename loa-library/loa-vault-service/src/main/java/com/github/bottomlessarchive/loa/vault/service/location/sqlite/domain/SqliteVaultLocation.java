package com.github.bottomlessarchive.loa.vault.service.location.sqlite.domain;

import com.github.bottomlessarchive.loa.vault.service.location.VaultLocation;
import com.github.bottomlessarchive.loa.vault.service.location.sqlite.service.SqliteConnectionManager;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;

/**
 * A {@link VaultLocation} implementation that stores document contents in SQLite database files.
 */
@RequiredArgsConstructor
public class SqliteVaultLocation implements VaultLocation {

    private final int vaultFileNumber;
    private final String documentId;
    private final SqliteConnectionManager connectionManager;

    @Override
    public void upload(final InputStream documentContents, final long contentLength) {
        connectionManager.insertDocument(vaultFileNumber, documentId, documentContents);
    }

    @Override
    public InputStream download() {
        return connectionManager.readDocument(vaultFileNumber, documentId);
    }

    @Override
    public boolean populated() {
        return connectionManager.documentExists(vaultFileNumber, documentId);
    }

    @Override
    public void clear() {
        connectionManager.deleteDocument(vaultFileNumber, documentId);
    }
}
