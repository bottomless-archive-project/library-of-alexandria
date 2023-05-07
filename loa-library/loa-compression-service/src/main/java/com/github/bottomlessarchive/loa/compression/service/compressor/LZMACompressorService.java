package com.github.bottomlessarchive.loa.compression.service.compressor;

import com.github.bottomlessarchive.loa.compression.domain.CompressionException;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class LZMACompressorService implements CompressorService {

    @Override
    public void compress(final Path originalLocation, final Path compressedLocation) {
        try (InputStream documentContent = Files.newInputStream(originalLocation);
             LZMACompressorOutputStream lzmaOutputStream = new LZMACompressorOutputStream(Files.newOutputStream(compressedLocation))) {
            IOUtils.copy(documentContent, lzmaOutputStream);
        } catch (final IOException e) {
            throw new CompressionException("Error while compressing document!", e);
        }
    }

    @Override
    public InputStream decompress(final InputStream compressedDocumentContent) {
        try {
            return new LZMACompressorInputStream(compressedDocumentContent);
        } catch (final IOException e) {
            throw new CompressionException("Error while decompressing document!", e);
        }
    }
}
