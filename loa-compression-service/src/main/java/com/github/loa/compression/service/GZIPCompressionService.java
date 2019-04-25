package com.github.loa.compression.service;

import com.github.loa.compression.domain.DocumentCompression;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Service
@ConditionalOnProperty(value = "loa.compression.algorithm", havingValue = "gzip")
public class GZIPCompressionService implements CompressionService {

    @Override
    public void compress(final InputStream documentContent, final OutputStream compressedDocumentContent) {
        try (GzipCompressorOutputStream out = new GzipCompressorOutputStream(compressedDocumentContent)) {
            IOUtils.copy(documentContent, out);
        } catch (IOException e) {
            throw new RuntimeException("Error while compressing document!", e);
        }
    }

    @Override
    public void decompress(final InputStream compressedDocumentContent, OutputStream decompressedDocumentContent) {
        try (GzipCompressorInputStream in = new GzipCompressorInputStream(compressedDocumentContent)) {
            IOUtils.copy(in, decompressedDocumentContent);
        } catch (IOException e) {
            throw new RuntimeException("Error while decompressing document!", e);
        }
    }
}
