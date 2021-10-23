package com.github.bottomlessarchive.loa.compression.service;

import com.github.bottomlessarchive.loa.compression.domain.CompressionException;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class GZIPCompressionService implements CompressionService {

    @Override
    public byte[] compress(final byte[] compressedDocumentContent) {
        try {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final GzipCompressorOutputStream gzipOutputStream = new GzipCompressorOutputStream(byteArrayOutputStream);

            gzipOutputStream.write(compressedDocumentContent);
            gzipOutputStream.close();

            return byteArrayOutputStream.toByteArray();
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
