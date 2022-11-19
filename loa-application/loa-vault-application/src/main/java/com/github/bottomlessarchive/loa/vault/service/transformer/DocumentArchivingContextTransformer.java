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
                .id(UUID.fromString(documentArchivingMessage.getId()))
                .vault(vaultConfigurationProperties.name())
                .type(DocumentType.valueOf(documentArchivingMessage.getType()))
                .source(documentArchivingMessage.getSource())
                .sourceLocationId(documentArchivingMessage.getSourceLocationId()
                        .orElse(null)
                )
                .contentLength(documentArchivingMessage.getContentLength())
                .originalContentLength(documentArchivingMessage.getOriginalContentLength())
                .checksum(documentArchivingMessage.getChecksum())
                .versionNumber(vaultConfigurationProperties.versionNumber())
                .compression(DocumentCompression.valueOf(documentArchivingMessage.getCompression()))
                .build();
    }
}
