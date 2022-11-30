package com.github.bottomlessarchive.loa.downloader.service.document;

import com.github.bottomlessarchive.loa.checksum.service.ChecksumProvider;
import com.github.bottomlessarchive.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.bottomlessarchive.loa.downloader.service.document.domain.DocumentArchivingContext;
import com.github.bottomlessarchive.loa.file.FileManipulatorService;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentArchivingMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class DocumentArchivingMessageFactory {

    private final ChecksumProvider checksumProvider;
    private final FileManipulatorService fileManipulatorService;
    private final CompressionConfigurationProperties compressionConfigurationProperties;

    @SneakyThrows
    public DocumentArchivingMessage newDocumentArchivingMessage(final DocumentArchivingContext documentArchivingContext,
            final Path compressedContent) {
        return DocumentArchivingMessage.builder()
                .id(documentArchivingContext.getId().toString())
                .type(documentArchivingContext.getType().toString())
                .fromBeacon(documentArchivingContext.isFromBeacon())
                .source(documentArchivingContext.getSource())
                .sourceLocationId(documentArchivingContext.getSourceLocationId())
                .contentLength(fileManipulatorService.size(compressedContent))
                .originalContentLength(fileManipulatorService.size(documentArchivingContext.getContents()))
                .checksum(checksumProvider.checksum(fileManipulatorService.getInputStream(documentArchivingContext.getContents())))
                .compression(compressionConfigurationProperties.algorithm().toString())
                .build();
    }
}
