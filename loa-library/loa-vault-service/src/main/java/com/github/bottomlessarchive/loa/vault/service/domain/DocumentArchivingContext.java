package com.github.bottomlessarchive.loa.vault.service.domain;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class DocumentArchivingContext {

    private final UUID id;
    private final String vault;
    private final DocumentType type;
    private final String source;
    private final UUID sourceLocationId;
    private final int contentLength;
    private final byte[] content;
    private final int versionNumber;
}
