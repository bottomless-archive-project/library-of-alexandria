package com.github.bottomlessarchive.loa.compression.service.compressor;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.decoder.BrotliInputStream;
import com.aayushatharva.brotli4j.encoder.BrotliOutputStream;
import com.github.bottomlessarchive.loa.compression.domain.CompressionException;
import jakarta.annotation.PostConstruct;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class BrotliCompressorService implements CompressorService {

    @PostConstruct
    void setup() {
        Brotli4jLoader.ensureAvailability();
    }

    @Override
    public void compress(final Path originalLocation, final Path compressedLocation) {
        try (InputStream documentContent = Files.newInputStream(originalLocation);
             BrotliOutputStream brotliOutputStream = new BrotliOutputStream(Files.newOutputStream(compressedLocation))) {
            IOUtils.copy(documentContent, brotliOutputStream);
        } catch (final IOException e) {
            throw new CompressionException("Error while compressing document!", e);
        }
    }

    @Override
    public InputStream decompress(final InputStream compressedDocumentContent) {
        try {
            return new BrotliInputStream(compressedDocumentContent);
        } catch (final IOException e) {
            throw new CompressionException("Error while decompressing document!", e);
        }
    }
}
