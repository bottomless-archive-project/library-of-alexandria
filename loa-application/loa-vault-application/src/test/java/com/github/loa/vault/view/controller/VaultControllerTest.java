package com.github.loa.vault.view.controller;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.document.view.service.MediaTypeCalculator;
import com.github.loa.queue.artemis.service.ArtemisQueueManipulator;
import com.github.loa.queue.artemis.service.consumer.ClientConsumerExecutor;
import com.github.loa.queue.artemis.service.consumer.pool.QueueConsumerFactory;
import com.github.loa.queue.artemis.service.producer.ClientProducerExecutor;
import com.github.loa.queue.artemis.service.producer.pool.QueueProducerFactory;
import com.github.loa.vault.configuration.VaultConfigurationProperties;
import com.github.loa.vault.service.RecompressorService;
import com.github.loa.vault.service.VaultDocumentManager;
import com.github.loa.vault.service.listener.VaultQueueConsumer;
import com.github.loa.vault.service.listener.VaultQueueListener;
import com.github.loa.vault.view.request.domain.QueryDocumentRequest;
import com.github.loa.vault.view.request.domain.RecompressRequest;
import com.github.loa.vault.view.response.domain.QueryDocumentResponse;
import io.netty.handler.codec.json.JsonObjectDecoder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@EnableAutoConfiguration
@SpringBootTest(classes = VaultController.class)
class VaultControllerTest {

    private static final String TEST_DOCUMENT_ID = "123e4567-e89b-12d3-a456-556642440000";

    private static RSocketRequester requester;

    @MockBean
    private DocumentEntityFactory documentEntityFactory;

    @MockBean
    private VaultDocumentManager vaultDocumentManager;

    @MockBean
    private RecompressorService recompressorService;

    @MockBean
    private VaultConfigurationProperties vaultConfigurationProperties;

    @BeforeAll
    public static void setup(@Value("${spring.rsocket.server.port}") final int port) {
        requester = RSocketRequester.builder()
                .rsocketStrategies(
                        RSocketStrategies.builder()
                        .decoder(new Jackson2JsonDecoder())
                        .encoder(new Jackson2JsonEncoder())
                        .build()
                )
                .tcp("localhost", port);
    }

    @Test
    public void testQueryDocumentWhenDocumentNotFound() {
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Mono.empty());

        final Mono<QueryDocumentResponse> response = requester.route("queryDocument")
                .data(QueryDocumentRequest.builder()
                        .documentId(TEST_DOCUMENT_ID)
                        .build()
                )
                .retrieveMono(QueryDocumentResponse.class);

        StepVerifier.create(response)
                .expectErrorMessage("Document not found with id 123e4567-e89b-12d3-a456-556642440000 or already removed!")
                .verify();
    }

    @Test
    public void testQueryDocumentWhenDocumentIsInADifferentVault() {
        when(vaultConfigurationProperties.getName())
                .thenReturn("my-vault");
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(
                        Mono.just(
                                DocumentEntity.builder()
                                        .vault("different-vault")
                                        .build()
                        )
                );

        final Mono<QueryDocumentResponse> response = requester.route("queryDocument")
                .data(QueryDocumentRequest.builder()
                        .documentId(TEST_DOCUMENT_ID)
                        .build()
                )
                .retrieveMono(QueryDocumentResponse.class);

        StepVerifier.create(response)
                .expectErrorMessage("Document with id 123e4567-e89b-12d3-a456-556642440000 is available on a different vault!")
                .verify();
    }

    @Test
    public void testQueryDocumentWhenRequestIsSuccessful() {
        when(vaultConfigurationProperties.getName())
                .thenReturn("my-vault");
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .vault("my-vault")
                .type(DocumentType.PDF)
                .build();
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Mono.just(documentEntity));
        final Resource documentResource = new ByteArrayResource(new byte[]{0, 1, 2});
        when(vaultDocumentManager.readDocument(documentEntity))
                .thenReturn(documentResource);

        final Mono<QueryDocumentResponse> response = requester.route("queryDocument")
                .data(QueryDocumentRequest.builder()
                        .documentId(TEST_DOCUMENT_ID)
                        .build()
                )
                .retrieveMono(QueryDocumentResponse.class);

        StepVerifier.create(response)
                .consumeNextWith(result -> {
                    assertThat(result, is(notNullValue()));
                    assertThat(result.getPayload().length, is(3));
                    assertThat(result.getPayload()[0], is((byte) 0));
                    assertThat(result.getPayload()[1], is((byte) 1));
                    assertThat(result.getPayload()[2], is((byte) 2));
                })
                .verifyComplete();
    }

    /*
    @Test
    public void testRecompressWhenModificationsAreDisabled() {
        when(vaultConfigurationProperties.isModificationEnabled())
                .thenReturn(false);

        webTestClient.post()
                .uri("/document/{documentId}/recompress", TEST_DOCUMENT_ID)
                .body(
                        BodyInserters.fromValue(
                                RecompressRequest.builder()
                                        .compression(DocumentCompression.GZIP)
                                        .build()
                        )
                )
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
                .returnResult(Void.class)
                .getResponseBody()
                .blockFirst();
    }

    @Test
    public void testRecompressWhenDocumentNotFound() {
        when(vaultConfigurationProperties.isModificationEnabled())
                .thenReturn(true);
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/document/{documentId}/recompress", TEST_DOCUMENT_ID)
                .body(
                        BodyInserters.fromValue(
                                RecompressRequest.builder()
                                        .compression(DocumentCompression.GZIP)
                                        .build()
                        )
                )
                .exchange()
                .expectStatus().isNotFound()
                .returnResult(Void.class)
                .getResponseBody()
                .blockFirst();
    }

    @Test
    public void testRecompressWhenDocumentIsInADifferentVault() {
        when(vaultConfigurationProperties.isModificationEnabled())
                .thenReturn(true);
        when(vaultConfigurationProperties.getName())
                .thenReturn("my-vault");
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(
                        Mono.just(
                                DocumentEntity.builder()
                                        .vault("different-vault")
                                        .build()
                        )
                );

        webTestClient.post()
                .uri("/document/{documentId}/recompress", TEST_DOCUMENT_ID)
                .body(
                        BodyInserters.fromValue(
                                RecompressRequest.builder()
                                        .compression(DocumentCompression.GZIP)
                                        .build()
                        )
                )
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .returnResult(Void.class)
                .getResponseBody()
                .blockFirst();
    }

    @Test
    public void testRecompressWhenRequestIsSuccessful() {
        when(vaultConfigurationProperties.isModificationEnabled())
                .thenReturn(true);
        when(vaultConfigurationProperties.getName())
                .thenReturn("my-vault");
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .vault("my-vault")
                .build();
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Mono.just(documentEntity));

        webTestClient.post()
                .uri("/document/{documentId}/recompress", TEST_DOCUMENT_ID)
                .body(
                        BodyInserters.fromValue(
                                RecompressRequest.builder()
                                        .compression(DocumentCompression.GZIP)
                                        .build()
                        )
                )
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .returnResult(Void.class)
                .getResponseBody()
                .blockFirst();

        verify(recompressorService).recompress(documentEntity, DocumentCompression.GZIP);
    }

    @Test
    public void testRemoveWhenModificationsAreDisabled() {
        when(vaultConfigurationProperties.isModificationEnabled())
                .thenReturn(false);

        webTestClient.delete()
                .uri("/document/{documentId}", TEST_DOCUMENT_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
                .returnResult(Void.class)
                .getResponseBody()
                .blockFirst();
    }

    @Test
    public void testRemoveWhenDocumentNotFound() {
        when(vaultConfigurationProperties.isModificationEnabled())
                .thenReturn(true);
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/document/{documentId}", TEST_DOCUMENT_ID)
                .exchange()
                .expectStatus().isNotFound()
                .returnResult(Void.class)
                .getResponseBody()
                .blockFirst();
    }

    @Test
    public void testRemoveWhenDocumentIsInADifferentVault() {
        when(vaultConfigurationProperties.isModificationEnabled())
                .thenReturn(true);
        when(vaultConfigurationProperties.getName())
                .thenReturn("my-vault");
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(
                        Mono.just(
                                DocumentEntity.builder()
                                        .vault("different-vault")
                                        .build()
                        )
                );

        webTestClient.delete()
                .uri("/document/{documentId}", TEST_DOCUMENT_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .returnResult(Void.class)
                .getResponseBody()
                .blockFirst();
    }

    @Test
    public void testRemoveWhenRequestIsSuccessful() {
        when(vaultConfigurationProperties.isModificationEnabled())
                .thenReturn(true);
        when(vaultConfigurationProperties.getName())
                .thenReturn("my-vault");
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .id(UUID.fromString(TEST_DOCUMENT_ID))
                .vault("my-vault")
                .build();
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Mono.just(documentEntity));
        when(documentEntityFactory.removeDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Mono.empty());
        when(vaultDocumentManager.removeDocument(documentEntity))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/document/{documentId}", TEST_DOCUMENT_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .returnResult(Void.class)
                .getResponseBody()
                .blockFirst();

        verify(vaultDocumentManager).removeDocument(documentEntity);
        verify(documentEntityFactory).removeDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID));
    }
     */
}
