package com.github.bottomlessarchive.loa.compression.service;

import com.github.bottomlessarchive.loa.compression.domain.CompressionException;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.davidmoten.io.extras.IOUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class GZIPCompressionService implements CompressionService {

    @Override
    public InputStream compress(final InputStream compressedDocumentContent) {
        try {
            return IOUtil.gzip(compressedDocumentContent);
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
