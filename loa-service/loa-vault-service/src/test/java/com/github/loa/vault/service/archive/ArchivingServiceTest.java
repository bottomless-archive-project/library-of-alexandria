package com.github.loa.vault.service.archive;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.loa.vault.service.backend.service.VaultDocumentStorage;
import com.github.loa.vault.service.domain.DocumentArchivingContext;
import com.mongodb.MongoWriteException;
import com.mongodb.WriteError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArchivingServiceTest {

    private static final byte[] CONTENT = {1, 2, 3, 4, 5};

    @Mock
    private DocumentEntityFactory documentEntityFactory;

    @Mock
    private DocumentCreationContextFactory documentCreationContextFactory;

    @Mock
    private VaultDocumentStorage vaultDocumentStorage;

    @Captor
    private ArgumentCaptor<byte[]> documentContent;

    @InjectMocks
    private ArchivingService underTest;

    @Test
    void testWhenPersistingAValidDocument() {
        final DocumentArchivingContext documentArchivingContext = createDocumentArchivingContext();
        final DocumentCreationContext documentCreationContext = DocumentCreationContext.builder()
                .build();
        when(documentCreationContextFactory.newContext(documentArchivingContext))
                .thenReturn(Mono.just(documentCreationContext));
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .build();
        when(documentEntityFactory.newDocumentEntity(documentCreationContext))
                .thenReturn(Mono.just(documentEntity));

        final Mono<DocumentEntity> result = underTest.archiveDocument(documentArchivingContext);

        StepVerifier.create(result)
                .consumeNextWith(documentEntity1 -> assertThat(documentEntity1, is(documentEntity)))
                .verifyComplete();

        verify(vaultDocumentStorage).persistDocument(eq(documentEntity), documentContent.capture());
        assertThat(documentContent.getValue(), is(CONTENT));
        verify(documentCreationContextFactory).newContext(documentArchivingContext);
        verify(documentEntityFactory).newDocumentEntity(documentCreationContext);
    }

    @Test
    void testRetryWhenErrorHappensWhilePersisting() {
        final DocumentArchivingContext documentArchivingContext = createDocumentArchivingContext();
        final DocumentCreationContext documentCreationContext = DocumentCreationContext.builder()
                .build();
        when(documentCreationContextFactory.newContext(documentArchivingContext))
                .thenReturn(Mono.just(documentCreationContext));
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .build();
        when(documentEntityFactory.newDocumentEntity(documentCreationContext))
                .thenReturn(Mono.just(documentEntity));
        final MongoWriteException mongoWriteException = mock(MongoWriteException.class);
        final WriteError writeError = mock(WriteError.class);
        when(writeError.getCode())
                .thenReturn(11000);
        when(mongoWriteException.getError())
                .thenReturn(writeError);
        //Do a normal exception, then a retry happens and throw the mongo exception to stop the retries
        doThrow(new RuntimeException("Test exception"), mongoWriteException)
                .when(vaultDocumentStorage).persistDocument(any(), any());

        final Mono<DocumentEntity> result = underTest.archiveDocument(documentArchivingContext);

        StepVerifier.create(result)
                .verifyError(MongoWriteException.class);

        verify(vaultDocumentStorage, times(2)).persistDocument(any(), any());
    }

    private DocumentArchivingContext createDocumentArchivingContext() {
        return DocumentArchivingContext.builder()
                .content(CONTENT)
                .build();
    }
}
