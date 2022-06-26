package com.github.bottomlessarchive.loa.vault.service.archive;

import com.github.bottomlessarchive.loa.document.service.DocumentManipulator;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.bottomlessarchive.loa.vault.service.backend.service.VaultDocumentStorage;
import com.github.bottomlessarchive.loa.vault.service.domain.DocumentArchivingContext;
import com.mongodb.MongoWriteException;
import com.mongodb.WriteError;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArchivingServiceTest {

    private static final int DUPLICATE_DOCUMENT_ID_ERROR_CODE = 11000;

    private static final byte[] CONTENT = {1, 2, 3, 4, 5};
    private static final UUID DOCUMENT_ID = UUID.fromString("321e4567-e89b-12d3-a456-556642440000");
    private static final String SOURCE_LOCATION_ID = "123e4567-e89b-12d3-a456-556642440000";

    @Mock
    private DocumentEntityFactory documentEntityFactory;

    @Mock
    private DocumentCreationContextFactory documentCreationContextFactory;

    @Mock
    private VaultDocumentStorage vaultDocumentStorage;

    @Mock
    private DocumentManipulator documentManipulator;

    @Captor
    private ArgumentCaptor<InputStream> documentContent;

    @InjectMocks
    private ArchivingService underTest;

    @Test
    @SneakyThrows
    void testWhenPersistingAValidDocument() {
        final DocumentArchivingContext documentArchivingContext = createDocumentArchivingContext();
        final DocumentCreationContext documentCreationContext = DocumentCreationContext.builder()
                .build();
        when(documentCreationContextFactory.newContext(documentArchivingContext))
                .thenReturn(documentCreationContext);

        final DocumentEntity documentEntity = DocumentEntity.builder()
                .id(UUID.randomUUID())
                .build();
        when(documentEntityFactory.newDocumentEntity(documentCreationContext))
                .thenReturn(documentEntity);

        underTest.archiveDocument(documentArchivingContext);

        verify(vaultDocumentStorage).persistDocument(eq(documentEntity), documentContent.capture(), eq((long) CONTENT.length));
        assertThat(documentContent.getValue().readAllBytes(), is(CONTENT));
        verify(documentCreationContextFactory).newContext(documentArchivingContext);
        verify(documentEntityFactory).newDocumentEntity(documentCreationContext);
        verify(documentManipulator).markDownloaded(documentEntity.getId());
    }

    @Test
    void testRetryWhenErrorHappensWhilePersisting() {
        final DocumentArchivingContext documentArchivingContext = createDocumentArchivingContext();
        final DocumentCreationContext documentCreationContext = DocumentCreationContext.builder()
                .build();
        when(documentCreationContextFactory.newContext(documentArchivingContext))
                .thenReturn(documentCreationContext);

        final DocumentEntity documentEntity = DocumentEntity.builder()
                .build();
        when(documentEntityFactory.newDocumentEntity(documentCreationContext))
                .thenReturn(documentEntity);

        final MongoWriteException mongoWriteException = mock(MongoWriteException.class);
        final WriteError writeError = mock(WriteError.class);
        when(writeError.getCode())
                .thenReturn(DUPLICATE_DOCUMENT_ID_ERROR_CODE);
        when(mongoWriteException.getError())
                .thenReturn(writeError);
        //Do a normal exception, then a retry happens and throw the mongo exception to stop the retries
        doThrow(new RuntimeException("Test exception"), mongoWriteException)
                .when(vaultDocumentStorage).persistDocument(any(), any(), anyLong());
        final UUID duplicateOfId = UUID.fromString("a10bb054-2d2c-41e8-8d0d-752cc7f0c778");
        final DocumentEntity duplicateOf = DocumentEntity.builder()
                .id(duplicateOfId)
                .build();

        when(documentEntityFactory.getDocumentEntity("test-checksum", CONTENT.length, "PDF"))
                .thenReturn(Optional.of(duplicateOf));

        underTest.archiveDocument(documentArchivingContext);

        verify(vaultDocumentStorage, times(2)).persistDocument(any(), any(), anyLong());
        verify(documentEntityFactory).addSourceLocation(duplicateOfId, SOURCE_LOCATION_ID);
    }

    @Test
    void testDontSaveSourceLocationWhenItIsNotAvailableAndTheDocumentIsADuplicate() {
        final DocumentArchivingContext documentArchivingContext = DocumentArchivingContext.builder()
                .id(DOCUMENT_ID)
                .content(new ByteArrayInputStream(CONTENT))
                .contentLength(CONTENT.length)
                .sourceLocationId(null) // The source location is not available
                .type(DocumentType.PDF)
                .build();
        final DocumentCreationContext documentCreationContext = DocumentCreationContext.builder()
                .build();
        when(documentCreationContextFactory.newContext(documentArchivingContext))
                .thenReturn(documentCreationContext);
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .build();
        when(documentEntityFactory.newDocumentEntity(documentCreationContext))
                .thenReturn(documentEntity);
        final MongoWriteException mongoWriteException = mock(MongoWriteException.class);
        final WriteError writeError = mock(WriteError.class);
        when(writeError.getCode())
                .thenReturn(DUPLICATE_DOCUMENT_ID_ERROR_CODE);
        when(mongoWriteException.getError())
                .thenReturn(writeError);
        //Do a normal exception, then a retry happens and throw the mongo exception to stop the retries
        doThrow(new RuntimeException("Test exception"), mongoWriteException)
                .when(vaultDocumentStorage).persistDocument(any(), any(), anyLong());
        final UUID duplicateOfId = UUID.fromString("a10bb054-2d2c-41e8-8d0d-752cc7f0c778");
        final DocumentEntity duplicateOf = DocumentEntity.builder()
                .id(duplicateOfId)
                .build();
        when(documentEntityFactory.getDocumentEntity("test-checksum", CONTENT.length, "PDF"))
                .thenReturn(Optional.of(duplicateOf));

        underTest.archiveDocument(documentArchivingContext);

        verify(vaultDocumentStorage, times(2)).persistDocument(any(), any(), anyLong());
        verify(documentEntityFactory, never()).addSourceLocation(any(), anyString());
    }

    private DocumentArchivingContext createDocumentArchivingContext() {
        return DocumentArchivingContext.builder()
                .id(DOCUMENT_ID)
                .content(new ByteArrayInputStream(CONTENT))
                .contentLength(CONTENT.length)
                .sourceLocationId(SOURCE_LOCATION_ID)
                .type(DocumentType.PDF)
                .build();
    }
}
