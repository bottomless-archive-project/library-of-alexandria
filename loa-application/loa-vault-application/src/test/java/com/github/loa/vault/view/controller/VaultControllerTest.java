package com.github.loa.vault.view.controller;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.document.view.service.MediaTypeCalculator;
import com.github.loa.vault.configuration.VaultConfigurationProperties;
import com.github.loa.vault.service.RecompressorService;
import com.github.loa.vault.service.VaultDocumentManager;
import com.github.loa.vault.view.request.domain.RecompressRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(VaultController.class)
class VaultControllerTest {

    private static final String TEST_DOCUMENT_ID = "123e4567-e89b-12d3-a456-556642440000";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private DocumentEntityFactory documentEntityFactory;

    @MockBean
    private VaultDocumentManager vaultDocumentManager;

    @MockBean
    private RecompressorService recompressorService;

    @MockBean
    private MediaTypeCalculator mediaTypeCalculator;

    @MockBean
    private VaultConfigurationProperties vaultConfigurationProperties;

    @Test
    public void testQueryDocumentWhenDocumentNotFound() {
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/document/{documentId}", TEST_DOCUMENT_ID)
                .exchange()
                .expectStatus().isNotFound()
                .returnResult(Void.class)
                .getResponseBody()
                .blockFirst();
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

        webTestClient.get()
                .uri("/document/{documentId}", TEST_DOCUMENT_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .returnResult(Void.class)
                .getResponseBody()
                .blockFirst();
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
        when(mediaTypeCalculator.calculateMediaType(DocumentType.PDF))
                .thenReturn(MediaType.APPLICATION_PDF);

        final byte[] result = webTestClient.get()
                .uri("/document/{documentId}", TEST_DOCUMENT_ID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().cacheControl(CacheControl.noCache())
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .returnResult(byte[].class)
                .getResponseBody()
                .blockFirst();

        assertThat(result, is(notNullValue()));
        assertThat(result.length, is(3));
        assertThat(result[0], is((byte) 0));
        assertThat(result[1], is((byte) 1));
        assertThat(result[2], is((byte) 2));
    }

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
}
