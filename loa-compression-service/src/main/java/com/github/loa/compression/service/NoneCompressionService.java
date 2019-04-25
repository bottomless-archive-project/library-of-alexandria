package com.github.loa.compression.service;

import org.apache.commons.compress.utils.IOUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Service
@ConditionalOnProperty(value = "loa.compression.algorithm", havingValue = "none")
public class NoneCompressionService implements CompressionService {

    @Override
    public void compress(InputStream documentContent, OutputStream compressedDocumentContent) {
        try {
            IOUtils.copy(documentContent, compressedDocumentContent);
        } catch (IOException e) {
            throw new RuntimeException("Error while compressing document!", e);
        }
    }

    @Override
    public void decompress(InputStream compressedDocumentContent, OutputStream decompressedDocumentContent) {
        try {
            IOUtils.copy(compressedDocumentContent, decompressedDocumentContent);
        } catch (IOException e) {
            throw new RuntimeException("Error while decompressing document!", e);
        }
    }
}
