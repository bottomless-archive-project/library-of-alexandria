package com.github.bottomlessarchive.loa.vault.service.location.sqlite;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocation;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocationFactory;
import com.github.bottomlessarchive.loa.vault.service.location.sqlite.domain.SqliteVaultLocation;
import com.github.bottomlessarchive.loa.vault.service.location.sqlite.service.SqliteConnectionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * A factory that creates SQLite-backed {@link VaultLocation} instances for documents.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "loa.vault.location.type", havingValue = "sqlite", matchIfMissing = true)
public class SqliteVaultLocationFactory implements VaultLocationFactory {

    private final SqliteConnectionManager connectionManager;

    @Override
    public VaultLocation getLocation(final DocumentEntity documentEntity, final DocumentCompression compression) {
        final int fileNumber = documentEntity.getVaultFile() > 0
                ? documentEntity.getVaultFile()
                : connectionManager.getActiveVaultFileNumber();

        return new SqliteVaultLocation(fileNumber, documentEntity.getId().toString(), connectionManager);
    }

    @Override
    public long getAvailableSpace() {
        return connectionManager.getAvailableSpace();
    }

    @Override
    public int getActiveVaultFileNumber() {
        return connectionManager.getActiveVaultFileNumber();
    }
}
