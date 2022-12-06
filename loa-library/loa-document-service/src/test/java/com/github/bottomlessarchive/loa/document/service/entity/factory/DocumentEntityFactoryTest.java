package com.github.bottomlessarchive.loa.document.service.entity.factory;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.document.repository.DocumentRepository;
import com.github.bottomlessarchive.loa.document.repository.domain.DocumentDatabaseEntity;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import com.github.bottomlessarchive.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.bottomlessarchive.loa.document.service.entity.transformer.DocumentEntityTransformer;
import com.github.bottomlessarchive.loa.repository.service.HexConverter;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentEntityFactoryTest {

    @Mock
    private HexConverter hexConverter;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentEntityTransformer documentEntityTransformer;

    @Captor
    private ArgumentCaptor<DocumentDatabaseEntity> documentDatabaseEntityArgumentCaptor;

    @InjectMocks
    private DocumentEntityFactory documentEntityFactory;

    @Test
    void testNewDocumentEntity() {
        final UUID documentId = UUID.randomUUID();
        final DocumentCreationContext documentCreationContext = DocumentCreationContext.builder()
                .id(documentId)
                .vault("test-vault")
                .type(DocumentType.PDF)
                .status(DocumentStatus.CORRUPT)
                .checksum("12345")
                .fileSize(200)
                .versionNumber(5)
                .compression(DocumentCompression.GZIP)
                .source("test-source")
                .sourceLocationId(Optional.of("test-location-id"))
                .build();
        when(hexConverter.decode("12345"))
                .thenReturn(new byte[]{5, 6, 7});
        when(hexConverter.decode("test-location-id"))
                .thenReturn(new byte[]{7, 8, 9});
        final DocumentEntity transformedEntity = mock(DocumentEntity.class);
        when(documentEntityTransformer.transform(any()))
                .thenReturn(transformedEntity);

        final DocumentEntity result = documentEntityFactory.newDocumentEntity(documentCreationContext);

        assertThat(result).isEqualTo(transformedEntity);

        verify(documentRepository).insertDocument(documentDatabaseEntityArgumentCaptor.capture());
        final DocumentDatabaseEntity insertedDocumentDatabaseEntity = documentDatabaseEntityArgumentCaptor.getValue();
        assertThat(insertedDocumentDatabaseEntity.getId())
                .isEqualTo(documentId);
        assertThat(insertedDocumentDatabaseEntity.getVault())
                .isEqualTo("test-vault");
        assertThat(insertedDocumentDatabaseEntity.getType())
                .isEqualTo("PDF");
        assertThat(insertedDocumentDatabaseEntity.getStatus())
                .isEqualTo("CORRUPT");
        assertThat(insertedDocumentDatabaseEntity.getChecksum())
                .isEqualTo(new byte[]{5, 6, 7});
        assertThat(insertedDocumentDatabaseEntity.getFileSize())
                .isEqualTo(200);
        assertThat(insertedDocumentDatabaseEntity.getDownloaderVersion())
                .isEqualTo(5);
        assertThat(insertedDocumentDatabaseEntity.getCompression())
                .isEqualTo("GZIP");
        assertThat(insertedDocumentDatabaseEntity.getSource())
                .isEqualTo("test-source");
        assertThat(insertedDocumentDatabaseEntity.getSourceLocations().size())
                .isEqualTo(1);
        assertThat(new ArrayList<>(insertedDocumentDatabaseEntity.getSourceLocations()).get(0))
                .isEqualTo(new byte[]{7, 8, 9});
    }

    @Test
    void testNewDocumentEntityWhenSourceLocationIdDoesNotExists() {
        final UUID documentId = UUID.randomUUID();
        final DocumentCreationContext documentCreationContext = DocumentCreationContext.builder()
                .id(documentId)
                .vault("test-vault")
                .type(DocumentType.PDF)
                .status(DocumentStatus.CORRUPT)
                .checksum("12345")
                .fileSize(200)
                .versionNumber(5)
                .compression(DocumentCompression.GZIP)
                .source("test-source")
                .sourceLocationId(Optional.empty())
                .build();
        when(hexConverter.decode("12345"))
                .thenReturn(new byte[]{5, 6, 7});
        final DocumentEntity transformedEntity = mock(DocumentEntity.class);
        when(documentEntityTransformer.transform(any()))
                .thenReturn(transformedEntity);

        final DocumentEntity result = documentEntityFactory.newDocumentEntity(documentCreationContext);

        assertThat(result).isEqualTo(transformedEntity);

        verify(documentRepository).insertDocument(documentDatabaseEntityArgumentCaptor.capture());
        final DocumentDatabaseEntity insertedDocumentDatabaseEntity = documentDatabaseEntityArgumentCaptor.getValue();
        assertThat(insertedDocumentDatabaseEntity.getSourceLocations().size())
                .isEqualTo(0);
    }
}
