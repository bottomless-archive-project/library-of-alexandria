package com.github.bottomlessarchive.loa.vault.service.archive;

import com.github.bottomlessarchive.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.bottomlessarchive.loa.vault.service.domain.DocumentArchivingContext;
import com.github.bottomlessarchive.loa.vault.service.location.sqlite.service.SqliteConnectionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "loa.vault.location.type", havingValue = "sqlite")
public class SqliteDocumentCreationContextFactory extends DocumentCreationContextFactory {

    private final SqliteConnectionManager sqliteConnectionManager;

    @Override
    public DocumentCreationContext newContext(final DocumentArchivingContext documentArchivingContext) {
        return newContextBuilder(documentArchivingContext)
                .vaultFile(sqliteConnectionManager.assignVaultFileNumber(documentArchivingContext.id().toString()))
                .build();
    }
}
