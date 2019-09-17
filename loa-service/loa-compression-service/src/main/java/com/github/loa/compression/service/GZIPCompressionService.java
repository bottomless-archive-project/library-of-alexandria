package com.github.loa.compression.service;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Service
public class GZIPCompressionService implements CompressionService {

    @Override
    public OutputStream compress(final OutputStream compressedDocumentContent) {
        try {
            return new GzipCompressorOutputStream(compressedDocumentContent);
        } catch (IOException e) {
            throw new RuntimeException("Error while compressing document!", e);
        }
    }

    @Override
    public InputStream decompress(final InputStream compressedDocumentContent) {
        try {
            return new GzipCompressorInputStream(compressedDocumentContent);
        } catch (IOException e) {
            throw new RuntimeException("Error while decompressing document!", e);
        }
    }
}
