package com.github.loa.vault.service.transformer;

import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.queue.service.domain.message.DocumentArchivingMessage;
import com.github.loa.vault.service.domain.DocumentArchivingContext;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DocumentArchivingContextTransformer {

    public DocumentArchivingContext transform(final DocumentArchivingMessage documentArchivingMessage) {
        return DocumentArchivingContext.builder()
                .id(UUID.fromString(documentArchivingMessage.getId()))
                .type(DocumentType.valueOf(documentArchivingMessage.getType()))
                .source(documentArchivingMessage.getSource())
                .contentLength(documentArchivingMessage.getContentLength())
                .content(documentArchivingMessage.getContent())
                .build();
    }
}
