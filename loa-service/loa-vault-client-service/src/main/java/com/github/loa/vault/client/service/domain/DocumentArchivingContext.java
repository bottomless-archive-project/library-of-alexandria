package com.github.loa.vault.client.service.domain;

import com.github.loa.document.service.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;
import java.util.UUID;

@Getter
@Builder
public class DocumentArchivingContext {

    private UUID id;
    private DocumentType type;
    private String source;
    private Path contents;
}
