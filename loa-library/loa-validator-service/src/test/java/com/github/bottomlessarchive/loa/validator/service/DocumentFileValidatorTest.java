package com.github.bottomlessarchive.loa.validator.service;

import com.github.bottomlessarchive.loa.parser.domain.ParsingResult;
import com.github.bottomlessarchive.loa.parser.service.DocumentDataParser;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.bottomlessarchive.loa.validator.configuration.FileValidationConfigurationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentFileValidatorTest {

    private static final long MAX_FILE_SIZE_IN_BYTES = 2048;
    private static final UUID DOCUMENT_ID = UUID.fromString("123e4567-e89b-12d3-a456-556642440000");
    private static final DocumentType DOCUMENT_TYPE = DocumentType.DOC;

    @Mock
    private FileValidationConfigurationProperties fileValidationConfigurationProperties;

    @Mock
    private DocumentDataParser documentDataParser;

    @InjectMocks
    private DocumentFileValidator underTest;

    @Test
    void testIsValidDocumentWhenDocumentIsTooSmall() {
        final StageLocation stageLocation = mock(StageLocation.class);

        when(stageLocation.exists())
                .thenReturn(true);
        when(stageLocation.size())
                .thenReturn(0L);

        final boolean result = underTest.isValidDocument(DOCUMENT_ID, stageLocation, DOCUMENT_TYPE);

        assertThat(result).isFalse();
    }

    @Test
    void testIsValidDocumentWhenDocumentIsTooBig() {
        final StageLocation stageLocation = mock(StageLocation.class);

        when(stageLocation.exists())
                .thenReturn(true);
        when(stageLocation.size())
                .thenReturn(MAX_FILE_SIZE_IN_BYTES + 1);
        when(fileValidationConfigurationProperties.maximumArchiveSize())
                .thenReturn(MAX_FILE_SIZE_IN_BYTES);

        final boolean result = underTest.isValidDocument(DOCUMENT_ID, stageLocation, DOCUMENT_TYPE);

        assertThat(result).isFalse();
    }

    @Test
    void testIsValidDocumentWhenDocumentIsntParsable() {
        final StageLocation stageLocation = mock(StageLocation.class);

        when(stageLocation.exists())
                .thenReturn(true);
        when(stageLocation.size())
                .thenReturn(MAX_FILE_SIZE_IN_BYTES - 1);
        when(fileValidationConfigurationProperties.maximumArchiveSize())
                .thenReturn(MAX_FILE_SIZE_IN_BYTES);

        final InputStream documentInputStream = mock(InputStream.class);
        when(stageLocation.openStream()).thenReturn(documentInputStream);
        when(documentDataParser.parseDocumentMetadata(DOCUMENT_ID, DOCUMENT_TYPE, documentInputStream))
                .thenThrow(new RuntimeException("Some error!"));

        final boolean result = underTest.isValidDocument(DOCUMENT_ID, stageLocation, DOCUMENT_TYPE);

        assertThat(result).isFalse();
        verify(documentDataParser).parseDocumentMetadata(DOCUMENT_ID, DOCUMENT_TYPE, documentInputStream);
    }

    @Test
    void testIsValidDocumentWhenDocumentIsValid() {
        final StageLocation stageLocation = mock(StageLocation.class);

        when(stageLocation.exists())
                .thenReturn(true);
        when(stageLocation.size())
                .thenReturn(MAX_FILE_SIZE_IN_BYTES - 1);
        when(fileValidationConfigurationProperties.maximumArchiveSize())
                .thenReturn(MAX_FILE_SIZE_IN_BYTES);

        final InputStream documentInputStream = mock(InputStream.class);
        when(stageLocation.openStream()).thenReturn(documentInputStream);
        when(documentDataParser.parseDocumentMetadata(DOCUMENT_ID, DOCUMENT_TYPE, documentInputStream))
                .thenReturn(
                        ParsingResult.builder()
                                .build()
                );

        final boolean result = underTest.isValidDocument(DOCUMENT_ID, stageLocation, DOCUMENT_TYPE);

        assertThat(result).isTrue();
    }
}
