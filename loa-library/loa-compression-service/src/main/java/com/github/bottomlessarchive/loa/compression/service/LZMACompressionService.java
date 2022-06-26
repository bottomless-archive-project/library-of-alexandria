package com.github.bottomlessarchive.loa.compression.service;

import com.github.bottomlessarchive.loa.compression.domain.CompressionException;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@Service
public class LZMACompressionService implements CompressionService {

    @Override
    public Path compress(final Path compressedDocumentContent) {
        /*try {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final LZMACompressorOutputStream gzipOutputStream = new LZMACompressorOutputStream(byteArrayOutputStream);

            gzipOutputStream.write(compressedDocumentContent);
            gzipOutputStream.close();

            return byteArrayOutputStream.toByteArray();
        } catch (final IOException e) {
            throw new CompressionException("Error while compressing document!", e);
        }*/
        return null; //TODO
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
