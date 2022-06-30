package com.github.bottomlessarchive.loa.compression.service.compressor.provider;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.compression.service.compressor.CompressorService;
import com.github.bottomlessarchive.loa.compression.service.compressor.GZIPCompressorService;
import com.github.bottomlessarchive.loa.compression.service.compressor.LZMACompressorService;
import com.github.bottomlessarchive.loa.compression.service.compressor.provider.domain.MissingCompressionServiceException;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * This service is responsible to provide a {@link CompressorService} implementation for a {@link DocumentCompression}.
 */
@Service
public class CompressorServiceProvider {

    private final Map<DocumentCompression, CompressorService> compressionServices;

    public CompressorServiceProvider(final GZIPCompressorService gzipCompressionService,
            final LZMACompressorService lzmaCompressionService) {
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
    public CompressorService getCompressionService(final DocumentCompression documentCompression) {
        if (!compressionServices.containsKey(documentCompression)) {
            throw new MissingCompressionServiceException("No compression service is registered for " + documentCompression + "!");
        }

        return compressionServices.get(documentCompression);
    }
}
