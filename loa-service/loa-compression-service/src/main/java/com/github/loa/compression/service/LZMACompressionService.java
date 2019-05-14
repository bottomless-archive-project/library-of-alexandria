package com.github.loa.compression.service;

import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Service
public class LZMACompressionService implements CompressionService {

    @Override
    public OutputStream compress(OutputStream compressedDocumentContent) {
        try {
            return new LZMACompressorOutputStream(compressedDocumentContent);
        } catch (IOException e) {
            throw new RuntimeException("Error while compressing document!", e);
        }
    }

    @Override
    public InputStream decompress(final InputStream compressedDocumentContent) {
        try {
            return new LZMACompressorInputStream(compressedDocumentContent);
        } catch (IOException e) {
            throw new RuntimeException("Error while decompressing document!", e);
        }
    }
}
