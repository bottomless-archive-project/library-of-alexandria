package com.github.bottomlessarchive.loa.compression.service.file;

import com.github.bottomlessarchive.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.compression.service.compressor.provider.CompressorServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class FileCompressionService {

    private final CompressorServiceProvider compressorServiceProvider;
    private final CompressionConfigurationProperties compressionConfigurationProperties;

    public Path compressDocument(final Path fileLocation) {
        return compressDocument(fileLocation, compressionConfigurationProperties.algorithm());
    }

    public Path compressDocument(final Path fileLocation, final DocumentCompression documentCompression) {
        if (documentCompression == DocumentCompression.NONE) {
            return fileLocation;
        }

        return compressorServiceProvider.getCompressionService(documentCompression)
                .compress(fileLocation);
    }
}
