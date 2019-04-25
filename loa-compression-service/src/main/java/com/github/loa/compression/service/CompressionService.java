package com.github.loa.compression.service;

import java.io.InputStream;
import java.io.OutputStream;

public interface CompressionService {

    void compress(InputStream documentContent, OutputStream compressedDocumentContent);

    void decompress(InputStream compressedDocumentContent, OutputStream decompressedDocumentContent);
}
