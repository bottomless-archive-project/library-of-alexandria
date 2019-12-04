package com.github.loa.source.file.service;

import com.github.loa.source.file.configuration.FileDocumentSourceConfigurationProperties;
import com.github.loa.source.file.service.domain.FileEncodingType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

@Service
@RequiredArgsConstructor
public class FileSourceFactory {

    private final FileDocumentSourceConfigurationProperties fileDocumentSourceConfigurationProperties;

    public InputStream newInputStream(final Path fileLocation) {
        try {
            final InputStream fileStream = Files.newInputStream(fileLocation);

            if (fileDocumentSourceConfigurationProperties.getEncoding() == FileEncodingType.GZIP) {
                return new GZIPInputStream(fileStream);
            }

            return fileStream;
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file input stream for file source!", e);
        }
    }
}
