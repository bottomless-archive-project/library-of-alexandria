package com.github.bottomlessarchive.loa.compression.service;

import com.github.bottomlessarchive.loa.compression.domain.CompressionException;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class GZIPCompressionService implements CompressionService {

    @Override
    public Path compress(final Path documentLocation) {
        final Path outputPath = documentLocation.getParent().resolve(documentLocation.getFileName() + ".gz");

        try (InputStream documentContent = Files.newInputStream(documentLocation);
             GzipCompressorOutputStream gzipOutputStream = new GzipCompressorOutputStream(Files.newOutputStream(outputPath))) {
            IOUtils.copy(documentContent, gzipOutputStream);
        } catch (final IOException e) {
            throw new CompressionException("Error while compressing document!", e);
        }

        return outputPath;
    }

    @Override
    public InputStream decompress(final InputStream compressedDocumentContent) {
        try {
            return new GzipCompressorInputStream(compressedDocumentContent);
        } catch (final IOException e) {
            throw new CompressionException("Error while decompressing document!", e);
        }
    }
}
