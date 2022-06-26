package com.github.bottomlessarchive.loa.vault.service.archive;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import com.github.bottomlessarchive.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.bottomlessarchive.loa.vault.service.domain.DocumentArchivingContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentCreationContextFactory {

    public DocumentCreationContext newContext(final DocumentArchivingContext documentArchivingContext) {
        return DocumentCreationContext.builder()
                .id(documentArchivingContext.getId())
                .vault(documentArchivingContext.getVault())
                .type(documentArchivingContext.getType())
                .status(DocumentStatus.CREATED)
                .source(documentArchivingContext.getSource())
                .sourceLocationId(documentArchivingContext.getSourceLocationId()
                        .orElse(null))
                .versionNumber(documentArchivingContext.getVersionNumber())
                .compression(documentArchivingContext.getCompression())
                .checksum(documentArchivingContext.getChecksum())
                .fileSize(documentArchivingContext.getOriginalContentLength())
                .build();
    }
}
