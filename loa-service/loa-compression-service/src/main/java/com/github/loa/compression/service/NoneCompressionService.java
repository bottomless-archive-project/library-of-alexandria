package com.github.loa.compression.service;

import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;

@Service
public class NoneCompressionService implements CompressionService {

    @Override
    public OutputStream compress(OutputStream compressedDocumentContent) {
        return compressedDocumentContent;
    }

    @Override
    public InputStream decompress(final InputStream compressedDocumentContent) {
        return compressedDocumentContent;
    }
}
