package com.github.loa.vault.service.domain;

import com.github.loa.document.service.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentArchivingContext {

    private DocumentType type;
    private String source;
    private byte[] content;
}
