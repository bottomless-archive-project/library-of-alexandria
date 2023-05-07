package com.github.bottomlessarchive.loa.compression.service.compressor;

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
public class GZIPCompressorService implements CompressorService {

    @Override
    public void compress(final Path originalLocation, final Path compressedLocation) {
        try (InputStream documentContent = Files.newInputStream(originalLocation);
             GzipCompressorOutputStream gzipOutputStream = new GzipCompressorOutputStream(Files.newOutputStream(compressedLocation))) {
            IOUtils.copy(documentContent, gzipOutputStream);
        } catch (final IOException e) {
            throw new CompressionException("Error while compressing document!", e);
        }
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
