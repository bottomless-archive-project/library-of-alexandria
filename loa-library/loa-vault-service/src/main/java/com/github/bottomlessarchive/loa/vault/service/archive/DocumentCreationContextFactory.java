package com.github.bottomlessarchive.loa.vault.service.archive;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import com.github.bottomlessarchive.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.bottomlessarchive.loa.vault.service.domain.DocumentArchivingContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(SqliteDocumentCreationContextFactory.class)
public class DocumentCreationContextFactory {

    public DocumentCreationContext newContext(final DocumentArchivingContext documentArchivingContext) {
        return newContextBuilder(documentArchivingContext)
                .build();
    }

    protected DocumentCreationContext.DocumentCreationContextBuilder newContextBuilder(
            final DocumentArchivingContext documentArchivingContext) {
        return DocumentCreationContext.builder()
                .id(documentArchivingContext.id())
                .vault(documentArchivingContext.vault())
                .type(documentArchivingContext.type())
                .status(DocumentStatus.CREATED)
                .source(documentArchivingContext.source())
                .sourceLocationId(documentArchivingContext.sourceLocationId())
                .versionNumber(documentArchivingContext.versionNumber())
                .compression(documentArchivingContext.compression())
                .checksum(documentArchivingContext.checksum())
                .fileSize(documentArchivingContext.originalContentLength());
    }
}
