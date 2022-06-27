package com.github.bottomlessarchive.loa.vault.service.backend.service;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocation;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocationFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VaultDocumentStorageTest {

    private static final InputStream CONTENT = new ByteArrayInputStream(new byte[]{0, 1, 2, 3, 4, -1, -2, -3, -4, -5});
    private static final InputStream COMPRESSED_CONTENT = new ByteArrayInputStream(new byte[]{111, 112, 113});

    @Mock
    private VaultLocationFactory vaultLocationFactory;

    @InjectMocks
    private VaultDocumentStorage underTest;

    @Test
    void testPersistDocumentUncompressed() {
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .compression(DocumentCompression.NONE)
                .build();
        final VaultLocation vaultLocation = mock(VaultLocation.class);

        underTest.persistDocument(documentEntity, CONTENT, vaultLocation, 100L);

        verify(vaultLocation).upload(CONTENT, 100L);
    }

    @Test
    void testPersistDocumentCompressed() {
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .compression(DocumentCompression.GZIP)
                .build();
        final VaultLocation vaultLocation = mock(VaultLocation.class);
        when(vaultLocation.getCompression())
                .thenReturn(Optional.of(DocumentCompression.GZIP));

        underTest.persistDocument(documentEntity, CONTENT, vaultLocation, 100L);

        verify(vaultLocation, never()).upload(CONTENT, 100L);
        verify(vaultLocation).upload(COMPRESSED_CONTENT, 100L);
    }

    @Test
    void testPersistDocumentWhenVaultLocationIsNotYetKnow() {
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .compression(DocumentCompression.NONE)
                .build();
        final VaultLocation vaultLocation = mock(VaultLocation.class);
        when(vaultLocationFactory.getLocation(documentEntity))
                .thenReturn(vaultLocation);

        underTest.persistDocument(documentEntity, CONTENT, 100L);

        verify(vaultLocation).upload(CONTENT, 100L);
    }
}
