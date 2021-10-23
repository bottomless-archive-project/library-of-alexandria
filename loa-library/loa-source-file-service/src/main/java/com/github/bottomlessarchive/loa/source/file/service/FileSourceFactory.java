package com.github.bottomlessarchive.loa.source.file.service;

import com.github.bottomlessarchive.loa.source.file.configuration.FileDocumentSourceConfigurationProperties;
import com.github.bottomlessarchive.loa.source.file.service.domain.FileEncodingType;
import com.github.bottomlessarchive.loa.source.file.service.domain.FileHandlingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

@Service
@RequiredArgsConstructor
public class FileSourceFactory {

    private final FileDocumentSourceConfigurationProperties fileDocumentSourceConfigurationProperties;

    public BufferedReader newSourceReader() {
        return new BufferedReader(new InputStreamReader(newInputStream(
                Path.of(fileDocumentSourceConfigurationProperties.getLocation()))));
    }

    private InputStream newInputStream(final Path fileLocation) {
        try {
            final InputStream fileStream = Files.newInputStream(fileLocation);

            if (fileDocumentSourceConfigurationProperties.getEncoding() == FileEncodingType.GZIP) {
                return new GZIPInputStream(fileStream);
            }

            return fileStream;
        } catch (final IOException e) {
            throw new FileHandlingException("Unable to create file input stream for file source!", e);
        }
    }
}
