package com.github.bottomlessarchive.loa.vault.service.transformer;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentArchivingMessage;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.bottomlessarchive.loa.vault.configuration.VaultConfigurationProperties;
import com.github.bottomlessarchive.loa.vault.service.domain.DocumentArchivingContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentArchivingContextTransformer {

    private final VaultConfigurationProperties vaultConfigurationProperties;

    public DocumentArchivingContext transform(final DocumentArchivingMessage documentArchivingMessage) {
        return DocumentArchivingContext.builder()
                .id(UUID.fromString(documentArchivingMessage.id()))
                .vault(vaultConfigurationProperties.name())
                .type(DocumentType.valueOf(documentArchivingMessage.type()))
                .fromBeacon(documentArchivingMessage.fromBeacon())
                .source(documentArchivingMessage.source())
                .sourceLocationId(documentArchivingMessage.sourceLocationId())
                .contentLength(documentArchivingMessage.contentLength())
                .originalContentLength(documentArchivingMessage.originalContentLength())
                .checksum(documentArchivingMessage.checksum())
                .versionNumber(vaultConfigurationProperties.versionNumber())
                .compression(DocumentCompression.valueOf(documentArchivingMessage.compression()))
                .build();
    }
}
