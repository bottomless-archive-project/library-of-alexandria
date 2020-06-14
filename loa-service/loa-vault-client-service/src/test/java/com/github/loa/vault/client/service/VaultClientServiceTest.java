package com.github.loa.vault.client.service;

import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.client.configuration.VaultClientConfigurationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.when;

/*
 In this test we need to use a real WebClient because mocking it (in a reasonable way) is next to impossible.
 */
@ExtendWith(MockitoExtension.class)
class VaultClientServiceTest {

    private static final UUID TEST_DOCUMENT_ID = UUID.fromString("123e4567-e89b-12d3-a456-556642440000");

    @Mock
    private VaultClientConfigurationProperties vaultClientConfigurationProperties;

    @Mock
    private DocumentManipulator documentManipulator;

    private VaultClientService underTest;

    @Test
    public void testQueryDocumentWhenUnableToGetDocumentContents() {
        final WebClient webClient = WebClient.builder()
                .exchangeFunction(clientRequest ->
                        Mono.just(
                                ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .header("Content-Type", "application/json")
                                        .body("{\"error\": \"Unable to get the content of a vault location!\"}")
                                        .build()
                        )
                )
                .build();
        underTest = new VaultClientService(vaultClientConfigurationProperties, documentManipulator, webClient);

        final DocumentEntity documentEntity = DocumentEntity.builder()
                .id(TEST_DOCUMENT_ID)
                .build();

        final Mono<Void> indexingFailureMono = Mono.empty();
        when(documentManipulator.markIndexFailure(TEST_DOCUMENT_ID))
                .thenReturn(indexingFailureMono);

        final Mono<byte[]> result = underTest.queryDocument(documentEntity);

        StepVerifier.create(result)
                .verifyComplete();

        StepVerifier.create(indexingFailureMono)
                .verifyComplete();
    }
}