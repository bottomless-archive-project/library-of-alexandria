package com.github.loa.downloader.download.service.file;

import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.downloader.command.configuration.DownloaderConfigurationProperties;
import com.github.loa.stage.service.StageLocationFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DocumentFileValidatorTest {

    private final static String DOCUMENT_ID = "123";
    private final static DocumentType DOCUMENT_TYPE = DocumentType.DOC;

    private final DownloaderConfigurationProperties downloaderConfigurationProperties =
            new DownloaderConfigurationProperties();

    private StageLocationFactory stageLocationFactory;
    private DocumentFileValidator underTest;

    @BeforeEach
    private void setup() {
        stageLocationFactory = mock(StageLocationFactory.class);

        underTest = new DocumentFileValidator(stageLocationFactory, downloaderConfigurationProperties);
    }

    @Test
    void testIsValidDocumentWhenDocumentIsTooSmall() {
        final File documentFile = mock(File.class);
        when(documentFile.length()).thenReturn(123L);
        when(stageLocationFactory.getLocation(DOCUMENT_ID, DOCUMENT_TYPE)).thenReturn(documentFile);

        final boolean result = underTest.isValidDocument(DOCUMENT_ID, DOCUMENT_TYPE);

        assertFalse(result);
    }

    @Test
    void testIsValidDocumentWhenDocumentIsTooBig() {
        final File documentFile = mock(File.class);
        when(documentFile.length()).thenReturn(8589934593L);
        when(stageLocationFactory.getLocation(DOCUMENT_ID, DOCUMENT_TYPE)).thenReturn(documentFile);

        final boolean result = underTest.isValidDocument(DOCUMENT_ID, DOCUMENT_TYPE);

        assertFalse(result);
    }

    @Test
    void testIsValidDocumentWhenDocumentIsGood() {
        final File documentFile = mock(File.class);
        when(documentFile.length()).thenReturn(2000L);
        when(stageLocationFactory.getLocation(DOCUMENT_ID, DOCUMENT_TYPE)).thenReturn(documentFile);

        final boolean result = underTest.isValidDocument(DOCUMENT_ID, DOCUMENT_TYPE);

        assertTrue(result);
    }
}
