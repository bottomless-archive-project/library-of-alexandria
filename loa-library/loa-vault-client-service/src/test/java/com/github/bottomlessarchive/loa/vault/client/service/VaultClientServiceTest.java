package com.github.bottomlessarchive.loa.vault.client.service;

import com.github.bottomlessarchive.loa.document.service.DocumentManipulator;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.vault.client.service.domain.VaultAccessException;
import com.github.bottomlessarchive.loa.vault.client.service.domain.VaultLocation;
import lombok.SneakyThrows;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VaultClientServiceTest {

    private static final UUID TEST_DOCUMENT_ID = UUID.fromString("123e4567-e89b-12d3-a456-556642440000");

    private VaultClientService vaultClientService;

    @Mock
    private OkHttpClient okHttpClient;

    @BeforeEach
    void setup() {
        vaultClientService = new VaultClientService(okHttpClient,
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
        final RSocketRequester.RequestSpec requestSpec = mock(RSocketRequester.RequestSpec.class);
        when(rSocketRequester.route("queryDocument"))
                .thenReturn(requestSpec);
        when(requestSpec.data(any()))
                .thenReturn(requestSpec);
        final DataBuffer dataBuffer = mock(DataBuffer.class);
        when(requestSpec.retrieveFlux(DataBuffer.class))
                .thenReturn(Flux.just(dataBuffer));

        final DocumentEntity documentEntity = DocumentEntity.builder()
                .id(TEST_DOCUMENT_ID)
                .vault("default")
                .build();

        final Flux<DataBuffer> result = vaultClientService.queryDocument(documentEntity);

        StepVerifier.create(result)
                .consumeNextWith(documentContents -> assertThat(documentContents, is(dataBuffer)))
                .verifyComplete();

        verify(documentManipulator, never()).markCorrupt(TEST_DOCUMENT_ID);
    }
}
