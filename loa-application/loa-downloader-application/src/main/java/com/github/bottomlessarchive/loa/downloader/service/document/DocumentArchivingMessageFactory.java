package com.github.bottomlessarchive.loa.downloader.service.document;

import com.github.bottomlessarchive.loa.checksum.service.ChecksumProvider;
import com.github.bottomlessarchive.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.bottomlessarchive.loa.downloader.service.document.domain.DocumentArchivingContext;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentArchivingMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class DocumentArchivingMessageFactory {

    private final ChecksumProvider checksumProvider;
    private final CompressionConfigurationProperties compressionConfigurationProperties;

    @SneakyThrows
    public DocumentArchivingMessage newDocumentArchivingMessage(final DocumentArchivingContext documentArchivingContext,
            final Path compressedContent) {
        return DocumentArchivingMessage.builder()
                .id(documentArchivingContext.getId().toString())
                .type(documentArchivingContext.getType().toString())
                .source(documentArchivingContext.getSource())
                .sourceLocationId(documentArchivingContext.getSourceLocationId())
                .contentLength(Files.size(compressedContent))
                .originalContentLength(Files.size(documentArchivingContext.getContents()))
                .checksum(checksumProvider.checksum(Files.newInputStream(documentArchivingContext.getContents())))
                .compression(compressionConfigurationProperties.algorithm().toString())
                .build();
    }
}
