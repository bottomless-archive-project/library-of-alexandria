package com.github.bottomlessarchive.loa.vault.view.controller;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.document.service.DocumentManipulator;
import com.github.bottomlessarchive.loa.vault.service.RecompressorService;
import com.github.bottomlessarchive.loa.vault.service.VaultDocumentManager;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.vault.configuration.VaultConfigurationProperties;
import com.github.bottomlessarchive.loa.vault.service.backend.service.VaultDocumentStorage;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocation;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocationFactory;
import com.github.bottomlessarchive.loa.vault.view.request.domain.DeleteDocumentRequest;
import com.github.bottomlessarchive.loa.vault.view.request.domain.QueryDocumentRequest;
import com.github.bottomlessarchive.loa.vault.view.request.domain.RecompressDocumentRequest;
import com.github.bottomlessarchive.loa.vault.view.request.domain.ReplaceCorruptDocumentRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
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

    @MockBean
    private DocumentManipulator documentManipulator;

    @MockBean
    private VaultLocationFactory vaultLocationFactory;

    @MockBean
    private VaultDocumentStorage vaultDocumentStorage;

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
    void testQueryDocumentWhenDocumentNotFound() {
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Mono.empty());

        final Flux<DataBuffer> response = requester.route("queryDocument")
                .data(QueryDocumentRequest.builder()
                        .documentId(TEST_DOCUMENT_ID)
                        .build()
                )
                .retrieveFlux(DataBuffer.class);

        StepVerifier.create(response)
                .expectErrorMessage("Document not found with id 123e4567-e89b-12d3-a456-556642440000 or already removed!")
                .verify();
    }

    @Test
    void testQueryDocumentWhenDocumentIsInADifferentVault() {
        when(vaultConfigurationProperties.name())
                .thenReturn("my-vault");
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(
                        Mono.just(
                                DocumentEntity.builder()
                                        .vault("different-vault")
                                        .build()
                        )
                );

        final Flux<DataBuffer> response = requester.route("queryDocument")
                .data(QueryDocumentRequest.builder()
                        .documentId(TEST_DOCUMENT_ID)
                        .build()
                )
                .retrieveFlux(DataBuffer.class);

        StepVerifier.create(response)
                .expectErrorMessage("Document with id 123e4567-e89b-12d3-a456-556642440000 is available on a different vault!")
                .verify();
    }

    @Test
    void testQueryDocumentWhenRequestIsSuccessful() {
        when(vaultConfigurationProperties.name())
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

        final Flux<DataBuffer> response = requester.route("queryDocument")
                .data(QueryDocumentRequest.builder()
                        .documentId(TEST_DOCUMENT_ID)
                        .build()
                )
                .retrieveFlux(DataBuffer.class);

        StepVerifier.create(response)
                .consumeNextWith(result -> {
                    try {
                        final byte[] totalResult = result.asInputStream().readAllBytes();

                        assertThat(result, is(notNullValue()));
                        assertThat(totalResult.length, is(3));
                        assertThat(totalResult[0], is((byte) 0));
                        assertThat(totalResult[1], is((byte) 1));
                        assertThat(totalResult[2], is((byte) 2));
                    } catch (IOException e) {
                        log.error("Failed to get totalResult!", e);
                    }
                })
                .verifyComplete();
    }

    @Test
    void testRecompressWhenModificationsAreDisabled() {
        when(vaultConfigurationProperties.modificationEnabled())
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
    void testRecompressWhenDocumentNotFound() {
        when(vaultConfigurationProperties.modificationEnabled())
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
    void testRecompressWhenDocumentIsInADifferentVault() {
        when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(true);
        when(vaultConfigurationProperties.name())
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
    void testRecompressWhenRequestIsSuccessful() {
        when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(true);
        when(vaultConfigurationProperties.name())
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
    void testRemoveWhenModificationsAreDisabled() {
        when(vaultConfigurationProperties.modificationEnabled())
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
    void testRemoveWhenDocumentNotFound() {
        when(vaultConfigurationProperties.modificationEnabled())
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
    void testRemoveWhenDocumentIsInADifferentVault() {
        when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(true);
        when(vaultConfigurationProperties.name())
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
    void testRemoveWhenRequestIsSuccessful() {
        when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(true);
        when(vaultConfigurationProperties.name())
                .thenReturn("my-vault");
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .id(UUID.fromString(TEST_DOCUMENT_ID))
                .vault("my-vault")
                .build();
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Mono.just(documentEntity));
        when(documentEntityFactory.removeDocumentEntity(documentEntity))
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
        verify(documentEntityFactory).removeDocumentEntity(documentEntity);
    }

    @Test
    void testReplaceCorruptDocumentWhenModificationsAreDisabled() {
        when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(false);

        final Mono<Void> response = requester.route("replaceCorruptDocument")
                .data(ReplaceCorruptDocumentRequest.builder()
                        .documentId(TEST_DOCUMENT_ID)
                        .content(new byte[]{})
                        .build()
                )
                .retrieveMono(Void.class);

        StepVerifier.create(response)
                .expectErrorMessage("Modification is disabled on this vault instance!")
                .verify();
    }

    @Test
    void testReplaceCorruptDocumentWhenDocumentIsInADifferentVault() {
        when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(true);
        when(vaultConfigurationProperties.name())
                .thenReturn("my-vault");
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(
                        Mono.just(
                                DocumentEntity.builder()
                                        .vault("different-vault")
                                        .build()
                        )
                );

        final Mono<Void> response = requester.route("replaceCorruptDocument")
                .data(ReplaceCorruptDocumentRequest.builder()
                        .documentId(TEST_DOCUMENT_ID)
                        .content(new byte[]{})
                        .build()
                )
                .retrieveMono(Void.class);

        StepVerifier.create(response)
                .expectErrorMessage("Document with id 123e4567-e89b-12d3-a456-556642440000 is available on a different vault!")
                .verify();
    }

    @Test
    void testReplaceCorruptDocumentReplacesTheDocument() {
        when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(true);
        when(vaultConfigurationProperties.name())
                .thenReturn("my-vault");
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .id(UUID.fromString(TEST_DOCUMENT_ID))
                .vault("my-vault")
                .compression(DocumentCompression.GZIP)
                .build();
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Mono.just(documentEntity));
        when(vaultDocumentManager.removeDocument(documentEntity))
                .thenReturn(Mono.just(documentEntity));
        final VaultLocation vaultLocation = mock(VaultLocation.class);
        when(vaultLocationFactory.getLocation(documentEntity, documentEntity.getCompression()))
                .thenReturn(vaultLocation);
        when(documentManipulator.markDownloaded(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Mono.empty());

        final byte[] newDocumentContent = {1, 2, 3, 4};

        final Mono<Void> response = requester.route("replaceCorruptDocument")
                .data(ReplaceCorruptDocumentRequest.builder()
                        .documentId(TEST_DOCUMENT_ID)
                        .content(newDocumentContent)
                        .build()
                )
                .retrieveMono(Void.class);

        StepVerifier.create(response)
                .verifyComplete();

        verify(vaultDocumentStorage)
                .persistDocument(documentEntity, newDocumentContent, vaultLocation);
        verify(documentManipulator)
                .markDownloaded(UUID.fromString(TEST_DOCUMENT_ID));
    }
}
