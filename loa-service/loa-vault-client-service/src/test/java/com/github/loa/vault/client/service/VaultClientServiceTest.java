package com.github.loa.vault.client.service;

import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.client.service.response.QueryDocumentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
    private DocumentManipulator documentManipulator;

    @Mock
    private RSocketRequester rSocketRequester;

    @BeforeEach
    void setup() {
        vaultClientService = new VaultClientService(documentManipulator, Map.of("default", rSocketRequester));
    }

    @Test
    public void testQueryDocumentWhenUnableToGetDocumentContents() {
        final RSocketRequester.RequestSpec requestSpec = mock(RSocketRequester.RequestSpec.class);
        when(rSocketRequester.route("queryDocument"))
                .thenReturn(requestSpec);
        when(requestSpec.data(any()))
                .thenReturn(requestSpec);
        when(requestSpec.retrieveMono(QueryDocumentResponse.class))
                .thenReturn(Mono.error(new RuntimeException("Unable to get the content of a vault location!")));

        final DocumentEntity documentEntity = DocumentEntity.builder()
                .id(TEST_DOCUMENT_ID)
                .vault("default")
                .build();
        final Mono<Void> indexingFailureMono = Mono.empty();
        when(documentManipulator.markIndexFailure(TEST_DOCUMENT_ID))
                .thenReturn(indexingFailureMono);

        final Mono<byte[]> result = vaultClientService.queryDocument(documentEntity);

        StepVerifier.create(result)
                .verifyComplete();

        StepVerifier.create(indexingFailureMono)
                .verifyComplete();
    }

    @Test
    public void testQueryDocumentWhenTheRequestWasSuccessful() {
        final RSocketRequester.RequestSpec requestSpec = mock(RSocketRequester.RequestSpec.class);
        when(rSocketRequester.route("queryDocument"))
                .thenReturn(requestSpec);
        when(requestSpec.data(any()))
                .thenReturn(requestSpec);
        when(requestSpec.retrieveMono(QueryDocumentResponse.class))
                .thenReturn(
                        Mono.just(
                                QueryDocumentResponse.builder()
                                        .payload(new byte[]{0, 1, 2})
                                        .build()
                        )
                );

        final DocumentEntity documentEntity = DocumentEntity.builder()
                .id(TEST_DOCUMENT_ID)
                .vault("default")
                .build();

        final Mono<byte[]> result = vaultClientService.queryDocument(documentEntity);

        StepVerifier.create(result)
                .consumeNextWith(documentContents -> assertThat(documentContents, is(equalTo(new byte[]{0, 1, 2}))))
                .verifyComplete();

        verify(documentManipulator, never()).markIndexFailure(TEST_DOCUMENT_ID);
    }
}
