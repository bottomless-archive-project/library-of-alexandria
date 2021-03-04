package com.github.loa.vault.view.controller;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.vault.configuration.VaultConfigurationProperties;
import com.github.loa.vault.service.RecompressorService;
import com.github.loa.vault.service.VaultDocumentManager;
import com.github.loa.vault.view.request.domain.DeleteDocumentRequest;
import com.github.loa.vault.view.request.domain.QueryDocumentRequest;
import com.github.loa.vault.view.request.domain.RecompressDocumentRequest;
import com.github.loa.vault.view.response.domain.QueryDocumentResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
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

    @Test
    public void testRecompressWhenModificationsAreDisabled() {
        when(vaultConfigurationProperties.isModificationEnabled())
                .thenReturn(false);

        final Mono<Void> response = requester.route("recompressDocument")
                .data(RecompressDocumentRequest.builder()
                        .documentId(TEST_DOCUMENT_ID)
                        .compression(DocumentCompression.GZIP)
                        .build()
                )
                .retrieveMono(Void.class);

        StepVerifier.create(response)
                .expectErrorMessage("Modification is disabled on this vault instance!")
                .verify();
    }

    @Test
    public void testRecompressWhenDocumentNotFound() {
        when(vaultConfigurationProperties.isModificationEnabled())
                .thenReturn(true);
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Mono.empty());

        final Mono<Void> response = requester.route("recompressDocument")
                .data(RecompressDocumentRequest.builder()
                        .documentId(TEST_DOCUMENT_ID)
                        .compression(DocumentCompression.GZIP)
                        .build()
                )
                .retrieveMono(Void.class);

        StepVerifier.create(response)
                .expectErrorMessage("Document not found with id 123e4567-e89b-12d3-a456-556642440000!")
                .verify();
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

        final Mono<Void> response = requester.route("recompressDocument")
                .data(RecompressDocumentRequest.builder()
                        .documentId(TEST_DOCUMENT_ID)
                        .compression(DocumentCompression.GZIP)
                        .build()
                )
                .retrieveMono(Void.class);

        StepVerifier.create(response)
                .expectErrorMessage("Document with id 123e4567-e89b-12d3-a456-556642440000 is available on a different vault!")
                .verify();
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

        final Mono<Void> response = requester.route("recompressDocument")
                .data(RecompressDocumentRequest.builder()
                        .documentId(TEST_DOCUMENT_ID)
                        .compression(DocumentCompression.GZIP)
                        .build()
                )
                .retrieveMono(Void.class);

        StepVerifier.create(response)
                .verifyComplete();

        verify(recompressorService).recompress(documentEntity, DocumentCompression.GZIP);
    }

    @Test
    public void testRemoveWhenModificationsAreDisabled() {
        when(vaultConfigurationProperties.isModificationEnabled())
                .thenReturn(false);

        final Mono<Void> response = requester.route("deleteDocument")
                .data(DeleteDocumentRequest.builder()
                        .documentId(TEST_DOCUMENT_ID)
                        .build()
                )
                .retrieveMono(Void.class);

        StepVerifier.create(response)
                .expectErrorMessage("Modification is disabled on this vault instance!")
                .verify();
    }

    @Test
    public void testRemoveWhenDocumentNotFound() {
        when(vaultConfigurationProperties.isModificationEnabled())
                .thenReturn(true);
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Mono.empty());

        final Mono<Void> response = requester.route("deleteDocument")
                .data(DeleteDocumentRequest.builder()
                        .documentId(TEST_DOCUMENT_ID)
                        .build()
                )
                .retrieveMono(Void.class);

        StepVerifier.create(response)
                .expectErrorMessage("Document not found with id 123e4567-e89b-12d3-a456-556642440000 or already removed!")
                .verify();
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

        final Mono<Void> response = requester.route("deleteDocument")
                .data(DeleteDocumentRequest.builder()
                        .documentId(TEST_DOCUMENT_ID)
                        .build()
                )
                .retrieveMono(Void.class);

        StepVerifier.create(response)
                .expectErrorMessage("Document with id 123e4567-e89b-12d3-a456-556642440000 is available on a different vault!")
                .verify();
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

        final Mono<Void> response = requester.route("deleteDocument")
                .data(DeleteDocumentRequest.builder()
                        .documentId(TEST_DOCUMENT_ID)
                        .build()
                )
                .retrieveMono(Void.class);

        StepVerifier.create(response)
                .verifyComplete();

        verify(vaultDocumentManager).removeDocument(documentEntity);
        verify(documentEntityFactory).removeDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID));
    }
}
