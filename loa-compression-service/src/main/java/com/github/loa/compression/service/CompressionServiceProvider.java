package com.github.loa.compression.service;

import com.github.loa.compression.domain.DocumentCompression;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CompressionServiceProvider {

    private final Map<DocumentCompression, CompressionService> compressionServices;

    public CompressionServiceProvider(
            final NoneCompressionService noneCompressionService,
            final GZIPCompressionService gzipCompressionService,
            final LZMACompressionService lzmaCompressionService
    ) {
        compressionServices = Map.of(
                DocumentCompression.NONE, noneCompressionService,
                DocumentCompression.GZIP, gzipCompressionService,
                DocumentCompression.LZMA, lzmaCompressionService
        );
    }

    public CompressionService getCompressionService(final DocumentCompression documentCompression) {
        return compressionServices.get(documentCompression);
    }
}
