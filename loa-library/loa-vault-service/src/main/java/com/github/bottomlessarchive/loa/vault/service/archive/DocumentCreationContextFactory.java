package com.github.bottomlessarchive.loa.vault.service.archive;

import com.github.bottomlessarchive.loa.checksum.service.ChecksumProvider;
import com.github.bottomlessarchive.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import com.github.bottomlessarchive.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.bottomlessarchive.loa.vault.service.domain.DocumentArchivingContext;
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
                        .vault(documentArchivingContext.getVault())
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
