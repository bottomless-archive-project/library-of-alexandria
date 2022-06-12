package com.github.bottomlessarchive.loa.vault.service.archive;

import com.github.bottomlessarchive.loa.checksum.service.ChecksumProvider;
import com.github.bottomlessarchive.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import com.github.bottomlessarchive.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.bottomlessarchive.loa.vault.service.domain.DocumentArchivingContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentCreationContextFactory {

    private final ChecksumProvider checksumProvider;
    private final CompressionConfigurationProperties compressionConfigurationProperties;

    public DocumentCreationContext newContext(final DocumentArchivingContext documentArchivingContext) {
        final String checksum = checksumProvider.checksum(documentArchivingContext.getContent());

        return DocumentCreationContext.builder()
                .id(documentArchivingContext.getId())
                .vault(documentArchivingContext.getVault())
                .type(documentArchivingContext.getType())
                .status(DocumentStatus.CREATED)
                .source(documentArchivingContext.getSource())
                .sourceLocationId(documentArchivingContext.getSourceLocationId()
                        .orElse(null))
                .versionNumber(documentArchivingContext.getVersionNumber())
                .compression(compressionConfigurationProperties.algorithm())
                .checksum(checksum)
                .fileSize(documentArchivingContext.getContentLength())
                .build();
    }
}
