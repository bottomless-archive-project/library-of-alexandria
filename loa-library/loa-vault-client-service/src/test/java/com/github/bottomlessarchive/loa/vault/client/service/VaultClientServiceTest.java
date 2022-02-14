package com.github.bottomlessarchive.loa.vault.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.vault.client.service.domain.VaultAccessException;
import com.github.bottomlessarchive.loa.vault.client.service.domain.VaultLocation;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class VaultClientServiceTest {

    private static final UUID TEST_DOCUMENT_ID = UUID.fromString("123e4567-e89b-12d3-a456-556642440000");

    private VaultClientService vaultClientService;

    @Mock
    private OkHttpClient okHttpClient;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        vaultClientService = new VaultClientService(okHttpClient,
                objectMapper,
                Map.of("default",
                        VaultLocation.builder()
                                .location("testlocation")
                                .port(123)
                                .build()
                )
        );
    }

    @Test
    void testQueryDocumentWhenDocumentIsInDifferentVault() {
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .id(TEST_DOCUMENT_ID)
                .vault("default-2")
                .build();

        assertThrows(VaultAccessException.class, () -> vaultClientService.queryDocument(documentEntity));
    }

    @Test
    void testQueryDocumentWhenTheRequestWasSuccessful() {
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .id(TEST_DOCUMENT_ID)
                .vault("default")
                .build();

        final InputStream result = vaultClientService.queryDocument(documentEntity);

        assertThat(result).hasBinaryContent(new byte[]{});
    }
}
