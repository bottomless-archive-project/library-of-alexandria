package com.github.loa.vault.service.archive;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.loa.vault.service.backend.service.VaultDocumentStorage;
import com.github.loa.vault.service.domain.DocumentArchivingContext;
import com.mongodb.MongoWriteException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArchivingServiceTest {

    @Mock
    private DocumentEntityFactory documentEntityFactory;

    @Mock
    private DocumentCreationContextFactory documentCreationContextFactory;

    @Mock
    private VaultDocumentStorage vaultDocumentStorage;

    @InjectMocks
    private ArchivingService underTest;

    @Test
    void testRetryWhenErrorHappensWhilePersisting() {
        final DocumentArchivingContext documentArchivingContext = DocumentArchivingContext.builder()
                .content(new byte[]{})
                .build();
        final DocumentCreationContext documentCreationContext = DocumentCreationContext.builder()
                .build();
        when(documentCreationContextFactory.newContext(documentArchivingContext))
                .thenReturn(Mono.just(documentCreationContext));
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .build();
        when(documentEntityFactory.newDocumentEntity(documentCreationContext))
                .thenReturn(Mono.just(documentEntity));
        final MongoWriteException mongoWriteException = mock(MongoWriteException.class);
        when(mongoWriteException.getMessage()).thenReturn("E11000 duplicate key error");
        //Do a normal exception, then a retry happens and throw the mongo exception to stop the retries
        doThrow(new RuntimeException("Test exception"), mongoWriteException)
                .when(vaultDocumentStorage).persistDocument(any(), any());

        final Mono<DocumentEntity> result = underTest.archiveDocument(documentArchivingContext);

        StepVerifier.create(result)
                .verifyError(MongoWriteException.class);
        verify(vaultDocumentStorage, times(2)).persistDocument(any(), any());
    }
}