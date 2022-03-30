package com.github.bottomlessarchive.loa.source.file.service;

import com.github.bottomlessarchive.loa.file.FileManipulatorService;
import com.github.bottomlessarchive.loa.source.file.configuration.FileDocumentSourceConfigurationProperties;
import com.github.bottomlessarchive.loa.source.file.service.domain.FileEncodingType;
import com.github.bottomlessarchive.loa.source.file.service.domain.FileHandlingException;
import lombok.SneakyThrows;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileSourceFactoryTest {

    @Mock
    private FileManipulatorService fileManipulatorService;

    @Mock
    private FileDocumentSourceConfigurationProperties fileDocumentSourceConfigurationProperties;

    @InjectMocks
    private FileSourceFactory underTest;

    @Test
    @SneakyThrows
    void testNewSourceReaderWhenNoEncoding() {
        final String testFileLocation = "test-source.txt";
        when(fileDocumentSourceConfigurationProperties.location())
                .thenReturn(testFileLocation);
        when(fileManipulatorService.getInputStream(testFileLocation))
                .thenReturn(new ByteArrayInputStream("hello\nworld\nwe\nare\ntesting".getBytes()));

        final BufferedReader result = underTest.newSourceReader();

        final List<String> resultLines = result.lines().toList();

        assertThat(resultLines)
                .hasSize(5)
                .containsExactly("hello", "world", "we", "are", "testing");
    }

    @Test
    @SneakyThrows
    void testNewSourceReaderWhenGzipEncoding() {
        final String testFileLocation = "test-source.txt";
        when(fileDocumentSourceConfigurationProperties.location())
                .thenReturn(testFileLocation);
        when(fileDocumentSourceConfigurationProperties.encoding())
                .thenReturn(FileEncodingType.GZIP);
        when(fileManipulatorService.getInputStream(testFileLocation))
                .thenReturn(new ByteArrayInputStream(compress("hello\nworld\nwe\nare\ntesting".getBytes())));

        final BufferedReader result = underTest.newSourceReader();

        final List<String> resultLines = result.lines().toList();

        assertThat(resultLines)
                .hasSize(5)
                .containsExactly("hello", "world", "we", "are", "testing");
    }

    @Test
    @SneakyThrows
    void testNewSourceReaderWhenExceptionIsThrown() {
        final String testFileLocation = "test-source.txt";
        when(fileDocumentSourceConfigurationProperties.location())
                .thenReturn(testFileLocation);
        when(fileManipulatorService.getInputStream(testFileLocation))
                .thenThrow(new IOException());

        assertThrows(FileHandlingException.class, () -> underTest.newSourceReader());
    }

    @SneakyThrows
    private byte[] compress(final byte[] compressedDocumentContent) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final GzipCompressorOutputStream gzipOutputStream = new GzipCompressorOutputStream(byteArrayOutputStream);

        gzipOutputStream.write(compressedDocumentContent);
        gzipOutputStream.close();

        return byteArrayOutputStream.toByteArray();
    }
}
