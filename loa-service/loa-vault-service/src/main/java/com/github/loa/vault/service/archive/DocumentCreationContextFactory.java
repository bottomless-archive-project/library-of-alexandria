package com.github.loa.vault.service.archive;

import com.github.loa.checksum.service.ChecksumProvider;
import com.github.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.loa.vault.service.domain.DocumentArchivingContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DocumentCreationContextFactory {

    private final ChecksumProvider checksumProvider;
    private final CompressionConfigurationProperties compressionConfigurationProperties;

    public Mono<DocumentCreationContext> newContext(final DocumentArchivingContext documentArchivingContext) {
        return Mono.just(documentArchivingContext)
                .flatMap(archivingContext -> checksumProvider.checksum(archivingContext.getContent()))
                .map(checksum -> DocumentCreationContext.builder()
                        .id(documentArchivingContext.getId())
                        .type(documentArchivingContext.getType())
                        .status(DocumentStatus.DOWNLOADED)
                        .source(documentArchivingContext.getSource())
                        .versionNumber(documentArchivingContext.getVersionNumber())
                        .compression(compressionConfigurationProperties.getAlgorithm())
                        .checksum(checksum)
                        .fileSize(documentArchivingContext.getContentLength())
                        .build()
                );
    }
}
