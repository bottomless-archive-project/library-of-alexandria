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
@ConditionalOnProperty(value = "loa.compression.algorithm", havingValue = "lzma")
public class LZMACompressionService implements CompressionService {

    @Override
    public void compress(InputStream documentContent, OutputStream compressedDocumentContent) {
        try (LZMACompressorOutputStream out = new LZMACompressorOutputStream(compressedDocumentContent)) {
            IOUtils.copy(documentContent, out);
        } catch (IOException e) {
            throw new RuntimeException("Error while compressing document!", e);
        }
    }

    @Override
    public void decompress(InputStream compressedDocumentContent, OutputStream decompressedDocumentContent) {
        try (LZMACompressorInputStream in = new LZMACompressorInputStream(compressedDocumentContent)) {
            IOUtils.copy(in, decompressedDocumentContent);
        } catch (IOException e) {
            throw new RuntimeException("Error while decompressing document!", e);
        }
    }
}
