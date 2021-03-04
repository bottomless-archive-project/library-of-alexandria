package com.github.loa.vault.client.service;

import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.client.configuration.VaultClientConfigurationProperties;
import com.github.loa.vault.client.configuration.VaultClientLocationConfigurationProperties;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 In this test we need to use a real WebClient because mocking it (in a reasonable way) is next to impossible.
 */
@Disabled
@ExtendWith(MockitoExtension.class)
class VaultClientServiceTest {

    private static final UUID TEST_DOCUMENT_ID = UUID.fromString("123e4567-e89b-12d3-a456-556642440000");

    @Mock
    private VaultClientConfigurationProperties vaultClientConfigurationProperties;

    @Mock
    private DocumentManipulator documentManipulator;

    /*@Test
    public void testQueryDocumentWhenUnableToGetDocumentContents() {
        final WebClient webClient = WebClient.builder()
                .exchangeFunction(clientRequest -> {
                            assertThat(clientRequest.url().toString(), is(equalTo("http://myhost:1234/document/"
                                    + TEST_DOCUMENT_ID.toString())));

                            return Mono.just(
                                    ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
                                            .header("Content-Type", "application/json")
                                            .body("{\"error\": \"Unable to get the content of a vault location!\"}")
                                            .build()
                            );
                        }
                )
                .build();
        final VaultClientService underTest = new VaultClientService(
                vaultClientConfigurationProperties, documentManipulator, webClient);

        final DocumentEntity documentEntity = DocumentEntity.builder()
                .id(TEST_DOCUMENT_ID)
                .vault("default")
                .build();

        final VaultClientLocationConfigurationProperties vaultClientLocationConfigurationProperties =
                new VaultClientLocationConfigurationProperties();
        vaultClientLocationConfigurationProperties.setHost("myhost");
        vaultClientLocationConfigurationProperties.setPort(1234);
        when(vaultClientConfigurationProperties.getLocation("default"))
                .thenReturn(vaultClientLocationConfigurationProperties);

        final Mono<Void> indexingFailureMono = Mono.empty();
        when(documentManipulator.markIndexFailure(TEST_DOCUMENT_ID))
                .thenReturn(indexingFailureMono);

        final Mono<byte[]> result = underTest.queryDocument(documentEntity);

        StepVerifier.create(result)
                .verifyComplete();

        StepVerifier.create(indexingFailureMono)
                .verifyComplete();
    }

    @Test
    public void testQueryDocumentWhenTheRequestWasSuccessful() {
        final DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        final WebClient webClient = WebClient.builder()
                .exchangeFunction(clientRequest -> {
                            assertThat(clientRequest.url().toString(), is(equalTo("http://myhost:1234/document/"
                                    + TEST_DOCUMENT_ID.toString())));

                            return Mono.just(
                                    ClientResponse.create(HttpStatus.OK)
                                            .header("Content-Type", "application/octet-stream")
                                            .body(Flux.just(dataBufferFactory.wrap(new byte[]{0, 1, 2})))
                                            .build()
                            );
                        }
                )
                .build();
        final VaultClientService underTest = new VaultClientService(
                vaultClientConfigurationProperties, documentManipulator, webClient);

        final DocumentEntity documentEntity = DocumentEntity.builder()
                .id(TEST_DOCUMENT_ID)
                .vault("default")
                .build();

        final VaultClientLocationConfigurationProperties vaultClientLocationConfigurationProperties =
                new VaultClientLocationConfigurationProperties();
        vaultClientLocationConfigurationProperties.setHost("myhost");
        vaultClientLocationConfigurationProperties.setPort(1234);
        when(vaultClientConfigurationProperties.getLocation("default"))
                .thenReturn(vaultClientLocationConfigurationProperties);

        final Mono<byte[]> result = underTest.queryDocument(documentEntity);

        StepVerifier.create(result)
                .consumeNextWith(documentContents -> assertThat(documentContents, is(equalTo(new byte[]{0, 1, 2}))))
                .verifyComplete();

        verify(documentManipulator, never()).markIndexFailure(TEST_DOCUMENT_ID);
    }*/
}
