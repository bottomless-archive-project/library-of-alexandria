package com.github.bottomlessarchive.loa.vault.service.location.sqlite.domain;

import com.github.bottomlessarchive.loa.vault.service.location.sqlite.service.SqliteConnectionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SqliteVaultLocationTest {

    private static final int VAULT_FILE_NUMBER = 3;
    private static final String DOCUMENT_ID = "123e4567-e89b-12d3-a456-556642440000";

    @Mock
    private SqliteConnectionManager connectionManager;

    @Test
    void testUpload() {
        final SqliteVaultLocation underTest = new SqliteVaultLocation(VAULT_FILE_NUMBER, DOCUMENT_ID, connectionManager);
        final InputStream content = new ByteArrayInputStream(new byte[]{1, 2, 3});

        underTest.upload(content, 3L);

        verify(connectionManager).insertDocument(VAULT_FILE_NUMBER, DOCUMENT_ID, content);
    }

    @Test
    void testDownload() {
        final SqliteVaultLocation underTest = new SqliteVaultLocation(VAULT_FILE_NUMBER, DOCUMENT_ID, connectionManager);
        final InputStream expected = new ByteArrayInputStream(new byte[]{4, 5, 6});
        when(connectionManager.readDocument(VAULT_FILE_NUMBER, DOCUMENT_ID))
                .thenReturn(expected);

        final InputStream result = underTest.download();

        assertThat(result).isSameAs(expected);
    }

    @Test
    void testPopulatedWhenDocumentExists() {
        final SqliteVaultLocation underTest = new SqliteVaultLocation(VAULT_FILE_NUMBER, DOCUMENT_ID, connectionManager);
        when(connectionManager.documentExists(VAULT_FILE_NUMBER, DOCUMENT_ID))
                .thenReturn(true);

        assertThat(underTest.populated()).isTrue();
    }

    @Test
    void testPopulatedWhenDocumentDoesNotExist() {
        final SqliteVaultLocation underTest = new SqliteVaultLocation(VAULT_FILE_NUMBER, DOCUMENT_ID, connectionManager);
        when(connectionManager.documentExists(VAULT_FILE_NUMBER, DOCUMENT_ID))
                .thenReturn(false);

        assertThat(underTest.populated()).isFalse();
    }

    @Test
    void testClear() {
        final SqliteVaultLocation underTest = new SqliteVaultLocation(VAULT_FILE_NUMBER, DOCUMENT_ID, connectionManager);

        underTest.clear();

        verify(connectionManager).deleteDocument(VAULT_FILE_NUMBER, DOCUMENT_ID);
    }
}
