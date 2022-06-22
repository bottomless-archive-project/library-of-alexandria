package com.github.bottomlessarchive.loa.vault.service.transformer;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentArchivingMessage;
import com.github.bottomlessarchive.loa.vault.configuration.VaultConfigurationProperties;
import com.github.bottomlessarchive.loa.vault.service.domain.DocumentArchivingContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentArchivingContextTransformer {

    private final VaultConfigurationProperties vaultConfigurationProperties;

    public DocumentArchivingContext transform(final DocumentArchivingMessage documentArchivingMessage, final InputStream content) {
        return DocumentArchivingContext.builder()
                .id(UUID.fromString(documentArchivingMessage.getId()))
                .vault(vaultConfigurationProperties.name())
                .type(DocumentType.valueOf(documentArchivingMessage.getType()))
                .source(documentArchivingMessage.getSource())
                .sourceLocationId(documentArchivingMessage.getSourceLocationId()
                        .orElse(null)
                )
                .contentLength(documentArchivingMessage.getContentLength())
                .content(content)
                .versionNumber(vaultConfigurationProperties.versionNumber())
                .build();
    }
}
