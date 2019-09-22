package com.github.loa.compression.service.provider;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.compression.service.CompressionService;
import com.github.loa.compression.service.GZIPCompressionService;
import com.github.loa.compression.service.LZMACompressionService;
import com.github.loa.compression.service.provider.domain.MissingCompressionServiceException;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * This service is responsible to provide a {@link CompressionService} implementation for a {@link DocumentCompression}.
 */
@Service
public class CompressionServiceProvider {

    private final Map<DocumentCompression, CompressionService> compressionServices;

    public CompressionServiceProvider(final GZIPCompressionService gzipCompressionService,
            final LZMACompressionService lzmaCompressionService) {
        compressionServices = Map.of(
                DocumentCompression.GZIP, gzipCompressionService,
                DocumentCompression.LZMA, lzmaCompressionService
        );
    }

    /**
     * Return the compression service that implements the compress and decompress algorithms that belonging to the
     * provided {@link DocumentCompression}.
     *
     * @param documentCompression the compression to get the service for
     * @return the service that's able to do compression and decompression for the provided compression algorithm
     */
    public CompressionService getCompressionService(final DocumentCompression documentCompression) {
        if (!compressionServices.containsKey(documentCompression)) {
            throw new MissingCompressionServiceException("No compression service is registered for "
                    + documentCompression + "!");
        }

        return compressionServices.get(documentCompression);
    }
}
