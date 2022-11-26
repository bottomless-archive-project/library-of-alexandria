package com.github.bottomlessarchive.loa.vault.service.archive;

import com.github.bottomlessarchive.loa.document.service.DocumentManipulator;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.domain.DuplicateDocumentException;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.bottomlessarchive.loa.staging.service.client.StagingClient;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.bottomlessarchive.loa.vault.service.VaultDocumentManager;
import com.github.bottomlessarchive.loa.vault.service.domain.DocumentArchivingContext;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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

    private static final byte[] CONTENT = {1, 2, 3, 4, 5};
    private static final UUID DOCUMENT_ID = UUID.fromString("321e4567-e89b-12d3-a456-556642440000");
    private static final String SOURCE_LOCATION_ID = "123e4567-e89b-12d3-a456-556642440000";

    @Mock
    private DocumentCreationContextFactory documentCreationContextFactory;

    @Mock
    private VaultDocumentManager vaultDocumentManager;

    @Mock
    private DocumentManipulator documentManipulator;

    @Mock
    private DocumentEntityFactory documentEntityFactory;

    @Mock
    private StagingClient stagingClient;

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
        final InputStream content = new ByteArrayInputStream(CONTENT);
        when(stagingClient.grabFromStaging(DOCUMENT_ID))
                .thenReturn(content);

        final DocumentEntity documentEntity = DocumentEntity.builder()
                .id(UUID.randomUUID())
                .build();
        when(documentEntityFactory.newDocumentEntity(documentCreationContext))
                .thenReturn(documentEntity);

        underTest.archiveDocument(documentArchivingContext);

        verify(vaultDocumentManager).archiveDocument(eq(documentEntity), eq(documentArchivingContext), eq(content));
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

        final DuplicateDocumentException duplicateDocumentException = mock(DuplicateDocumentException.class);
        //Do a normal exception, then a retry happens and throw the mongo exception to stop the retries
        doThrow(new RuntimeException("Test exception"), duplicateDocumentException)
                .when(vaultDocumentManager).archiveDocument(any(), any(), any());
        final UUID duplicateOfId = UUID.fromString("a10bb054-2d2c-41e8-8d0d-752cc7f0c778");
        final DocumentEntity duplicateOf = DocumentEntity.builder()
                .id(duplicateOfId)
                .build();

        when(documentEntityFactory.getDocumentEntity("test-checksum", 8L, "PDF"))
                .thenReturn(Optional.of(duplicateOf));

        underTest.archiveDocument(documentArchivingContext);

        verify(vaultDocumentManager, times(2)).archiveDocument(any(), any(), any());
        verify(documentEntityFactory).addSourceLocation(duplicateOfId, SOURCE_LOCATION_ID);
        verify(stagingClient).deleteFromStaging(DOCUMENT_ID);
    }

    @Test
    void testDontSaveSourceLocationWhenItIsNotAvailableAndTheDocumentIsADuplicate() {
        final DocumentArchivingContext documentArchivingContext = DocumentArchivingContext.builder()
                .id(DOCUMENT_ID)
                .contentLength(CONTENT.length)
                .sourceLocationId(null) // The source location is not available
                .type(DocumentType.PDF)
                .checksum("test-checksum")
                .contentLength(6L)
                .originalContentLength(8L)
                .build();
        when(stagingClient.grabFromStaging(DOCUMENT_ID))
                .thenReturn(new ByteArrayInputStream(CONTENT));
        final DocumentCreationContext documentCreationContext = DocumentCreationContext.builder()
                .build();
        when(documentCreationContextFactory.newContext(documentArchivingContext))
                .thenReturn(documentCreationContext);
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .build();
        when(documentEntityFactory.newDocumentEntity(documentCreationContext))
                .thenReturn(documentEntity);
        final DuplicateDocumentException duplicateDocumentException = mock(DuplicateDocumentException.class);
        //Do a normal exception, then a retry happens and throw the mongo exception to stop the retries
        doThrow(new RuntimeException("Test exception"), duplicateDocumentException)
                .when(vaultDocumentManager).archiveDocument(any(), any(), any());
        final UUID duplicateOfId = UUID.fromString("a10bb054-2d2c-41e8-8d0d-752cc7f0c778");
        final DocumentEntity duplicateOf = DocumentEntity.builder()
                .id(duplicateOfId)
                .build();
        when(documentEntityFactory.getDocumentEntity("test-checksum", 8L, "PDF"))
                .thenReturn(Optional.of(duplicateOf));

        underTest.archiveDocument(documentArchivingContext);

        verify(vaultDocumentManager, times(2)).archiveDocument(any(), any(), any());
        verify(documentEntityFactory, never()).addSourceLocation(any(), anyString());
    }

    private DocumentArchivingContext createDocumentArchivingContext() {
        return DocumentArchivingContext.builder()
                .id(DOCUMENT_ID)
                .contentLength(CONTENT.length)
                .originalContentLength(8L)
                .sourceLocationId(SOURCE_LOCATION_ID)
                .type(DocumentType.PDF)
                .checksum("test-checksum")
                .build();
    }
}
