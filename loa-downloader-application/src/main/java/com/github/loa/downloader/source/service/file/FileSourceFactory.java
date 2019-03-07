package com.github.loa.downloader.source.service.file;

import com.github.loa.downloader.source.configuration.FileDocumentSourceConfiguration;
import com.github.loa.downloader.source.domain.file.FileEncodingType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.zip.GZIPInputStream;

@Service
@RequiredArgsConstructor
public class FileSourceFactory {

    private final FileDocumentSourceConfiguration fileDocumentSourceConfiguration;

    public InputStream newInputStream(final String fileLocation) {
        try {
            final InputStream fileStream = new FileInputStream(fileLocation);

            if (fileDocumentSourceConfiguration.getEncoding() == FileEncodingType.GZIP) {
                return new GZIPInputStream(fileStream);
            }

            return fileStream;
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file input stream for file source!", e);
        }
    }
}
