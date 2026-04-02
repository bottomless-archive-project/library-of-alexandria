package com.github.bottomlessarchive.loa.vault.service.location.sqlite;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocation;
import com.github.bottomlessarchive.loa.vault.service.location.sqlite.domain.SqliteVaultLocation;
import com.github.bottomlessarchive.loa.vault.service.location.sqlite.service.SqliteConnectionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SqliteVaultLocationFactoryTest {

    @Mock
    private SqliteConnectionManager connectionManager;

    @InjectMocks
    private SqliteVaultLocationFactory underTest;

    @Test
    void testGetLocationUsesVaultFileFromEntity() {
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .id(UUID.fromString("123e4567-e89b-12d3-a456-556642440000"))
                .type(DocumentType.PDF)
                .compression(DocumentCompression.NONE)
                .vaultFile(5)
                .build();

        final VaultLocation result = underTest.getLocation(documentEntity, DocumentCompression.NONE);

        assertThat(result).isInstanceOf(SqliteVaultLocation.class);
    }

    @Test
    void testGetAvailableSpace() {
        when(connectionManager.getAvailableSpace()).thenReturn(1000L);

        assertThat(underTest.getAvailableSpace()).isEqualTo(1000L);
    }
}
