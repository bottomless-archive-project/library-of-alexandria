package com.github.bottomlessarchive.loa.compression.service.file;

import com.github.bottomlessarchive.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.compression.service.compressor.provider.CompressorServiceProvider;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class FileCompressionService {

    private final CompressorServiceProvider compressorServiceProvider;
    private final CompressionConfigurationProperties compressionConfigurationProperties;

    public Path compressDocument(final Path fileLocation) {
        return compressDocument(fileLocation, compressionConfigurationProperties.algorithm());
    }

    @SneakyThrows
    public Path compressDocument(final Path fileLocation, final DocumentCompression documentCompression) {
        if (documentCompression == DocumentCompression.NONE) {
            return fileLocation;
        }

        final Path compressedFileLocation = Files.createTempFile(null, "." + documentCompression.getFileExtension());

        compressorServiceProvider.getCompressionService(documentCompression)
                .compress(fileLocation, compressedFileLocation);

        return compressedFileLocation;
    }
}
