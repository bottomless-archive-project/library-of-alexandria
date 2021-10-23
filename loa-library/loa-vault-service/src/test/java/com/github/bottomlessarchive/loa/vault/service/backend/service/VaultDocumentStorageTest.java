package com.github.bottomlessarchive.loa.vault.service.backend.service;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.compression.service.CompressionService;
import com.github.bottomlessarchive.loa.compression.service.provider.CompressionServiceProvider;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocation;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocationFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VaultDocumentStorageTest {

    private static final byte[] CONTENT = {0, 1, 2, 3, 4, -1, -2, -3, -4, -5};
    private static final byte[] COMPRESSED_CONTENT = {111, 112, 113};

    @Mock
    private VaultLocationFactory vaultLocationFactory;

    @Mock
    private CompressionServiceProvider compressionServiceProvider;

    @InjectMocks
    private VaultDocumentStorage underTest;

    @Test
    void testPersistDocumentUncompressed() {
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .compression(DocumentCompression.NONE)
                .build();
        final VaultLocation vaultLocation = mock(VaultLocation.class);

        underTest.persistDocument(documentEntity, CONTENT, vaultLocation);

        verify(vaultLocation).upload(CONTENT);
        verify(compressionServiceProvider, never()).getCompressionService(any());
    }

    @Test
    void testPersistDocumentCompressed() {
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .compression(DocumentCompression.GZIP)
                .build();
        final CompressionService compressionService = mock(CompressionService.class);
        when(compressionServiceProvider.getCompressionService(DocumentCompression.GZIP))
                .thenReturn(compressionService);
        when(compressionService.compress(CONTENT))
                .thenReturn(COMPRESSED_CONTENT);
        final VaultLocation vaultLocation = mock(VaultLocation.class);
        when(vaultLocation.getCompression())
                .thenReturn(Optional.of(DocumentCompression.GZIP));

        underTest.persistDocument(documentEntity, CONTENT, vaultLocation);

        verify(vaultLocation, never()).upload(CONTENT);
        verify(vaultLocation).upload(COMPRESSED_CONTENT);
        verify(compressionServiceProvider).getCompressionService(DocumentCompression.GZIP);
        verify(compressionService).compress(CONTENT);
    }

    @Test
    void testPersistDocumentWhenVaultLocationIsNotYetKnow() {
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .compression(DocumentCompression.NONE)
                .build();
        final VaultLocation vaultLocation = mock(VaultLocation.class);
        when(vaultLocationFactory.getLocation(documentEntity))
                .thenReturn(vaultLocation);

        underTest.persistDocument(documentEntity, CONTENT);

        verify(vaultLocation).upload(CONTENT);
    }
}
